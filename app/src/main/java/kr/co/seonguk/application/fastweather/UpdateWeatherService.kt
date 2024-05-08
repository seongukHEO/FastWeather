package kr.co.seonguk.application.fastweather

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kr.co.seonguk.application.fastweather.databinding.WidgetWeatherBinding

class UpdateWeatherService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //notification Channel을 만들고
        createChannel()
        startForeground(1, createNotification())

        //foreground Service를 해줘야한다
        val appWidgetManager : AppWidgetManager = AppWidgetManager.getInstance(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //위젯은 권한 없음 상태로 표시한다

            val pendingIntent: PendingIntent = Intent(this, SettingActivity::class.java).let {
                PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_IMMUTABLE)
            }

            RemoteViews(packageName, R.layout.widget_weather).apply {
                setTextViewText(R.id.temperatureTextview, "권한 없음")
                //pendingIntent로 넘어가게끔 설정
                setTextViewText(
                    R.id.weatherTextView,
                    ""
                )
                setOnClickPendingIntent(R.id.temperatureTextview, pendingIntent)

            }.also {remoteViews ->
                val appwidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(appwidgetName, remoteViews)
            }

            stopSelf()

            //권한이 없다면
            return super.onStartCommand(intent, flags, startId)
        }

        //여긴 권한이 있다
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            WeatherRepository.getVillageForecast(
                it.longitude,
                it.latitude,
                successCallback = { forecastList ->

                    val pendingServiceIntent: PendingIntent = Intent(this, UpdateWeatherService::class.java)
                        .let {intent ->
                            PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
                        }
                    val currentForecast = forecastList.first()
                    Log.e("sibal1234", currentForecast.toString())

                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperature1Textview,
                            getString(R.string.temperature_text, currentForecast.temperature)
                        )

                        Log.e("sibal1234", currentForecast.temperature.toString())
                        setTextViewText(
                            R.id.weather1TextView,
                            currentForecast.sky
                        )

                        setOnClickPendingIntent(R.id.temperature1Textview, pendingServiceIntent)
                    }.also {remoteViews ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }

                    stopSelf()
                },
                failureCallback = {
                    Log.e("sibal1234", it.toString())
                    val pendingServiceIntent: PendingIntent = Intent(this, UpdateWeatherService::class.java)
                        .let {intent ->
                            PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
                        }
                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperatureTextview,
                            "에러"
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            ""
                        )
                        setOnClickPendingIntent(R.id.temperatureTextview, pendingServiceIntent)
                    }.also {remoteViews ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }


                    stopSelf()
                }
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getRetrofit(){

    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val channel = NotificationChannel(
            "widget_refresh_channel",
            "날씨앱",
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }


    private fun createNotification() : Notification{

        return NotificationCompat.Builder(this, "widget_refresh_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("날씨앱")
            .setContentText("날씨 업데이트")
            .build()
    }


}
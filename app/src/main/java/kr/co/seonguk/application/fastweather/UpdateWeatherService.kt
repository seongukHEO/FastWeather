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

    lateinit var binding:WidgetWeatherBinding

    private val CHANNEL_ID = "widget_refresh_channel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        binding = WidgetWeatherBinding.inflate(LayoutInflater.from(this))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        createChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // 위치 정보 권한이 허용되어 있으면 위치 정보를 얻어오고, 권한이 없으면 서비스를 종료합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocationAndFetchWeather()
        } else {
            stopForeground(true)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                WeatherRepository.getVillageForecast(
                    location.longitude,
                    location.latitude,
                    successCallback = { forecastList ->
                        // 날씨 정보를 가져오는 작업 수행
                        // 이후 앱 위젯 업데이트 및 서비스 종료 처리
                        updateAppWidget(forecastList)
                        stopSelf()
                    },
                    failureCallback = {
                        // 날씨 정보를 가져오지 못했을 때의 처리
                        // 이후 앱 위젯 업데이트 및 서비스 종료 처리
                        updateAppWidgetWithError()
                        stopSelf()
                    }
                )
            } else {
                // 위치 정보를 가져오지 못했을 때의 처리
                // 이후 앱 위젯 업데이트 및 서비스 종료 처리
                updateAppWidgetWithError()
                stopSelf()
            }
        }
    }

    private fun updateAppWidget(forecastList: List<Forecast>) {
        // 날씨 정보를 받아와서 앱 위젯 업데이트하는 작업을 수행합니다.
        binding.temperature1Textview.text = forecastList.map { it.temperature }.toString()
        binding.weather1TextView.text = forecastList.map { it.sky }.toString()
    }

    private fun updateAppWidgetWithError() {
        // 날씨 정보를 가져오는 데 실패했을 때 앱 위젯을 에러 상태로 업데이트하는 작업을 수행합니다.
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "날씨앱",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("날씨앱")
            .setContentText("날씨 업데이트 중")
            .build()
    }
}

package kr.co.seonguk.application.fastweather

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WeatherAppWidgetProvider : AppWidgetProvider() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Foreground Service를 시작하기 전에 위치 액세스 권한이 있는지 확인
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 액세스 권한이 있을 경우에만 Foreground Service 시작
            startUpdateWeatherService(context)
        } else {
            // 위치 액세스 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

        // 위젯 갱신 코드
        appWidgetIds.forEach { appWidgetId ->
            val pendingIntent: PendingIntent = Intent(context, UpdateWeatherService::class.java).let {intent ->
                PendingIntent.getForegroundService(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
            }

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_weather
            ).apply {
                setOnClickPendingIntent(R.id.temperatureTextview, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun startUpdateWeatherService(context: Context) {
        val serviceIntent = Intent(context, UpdateWeatherService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}

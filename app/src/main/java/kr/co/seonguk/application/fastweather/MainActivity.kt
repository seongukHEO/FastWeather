package kr.co.seonguk.application.fastweather

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        val baseDate = BaseDateTime.getBaseDateTime()

        Log.e("test1234", "${baseDate.baseDate}, ${baseDate.baseTime}")

        service.getVillageForecast(
            "Qahe3YsG5DEh1NrEibW9IUu4P/yYTgk4lBC6o0giu4nI1UjwSA3iTZXm4OcQ4Z/Q1ALRTLaKfZ6DsMEg+XsoqA==",
            baseDate.baseDate,
            baseDate.baseTime,
            55,
            127
        ).enqueue(object :Callback<WeatherEntity>{
            override fun onResponse(p0: Call<WeatherEntity>, p1: Response<WeatherEntity>) {

                val forecastDateTimeMap = mutableMapOf<String, Forecast>()

                val forecastList = p1.body()?.response?.body?.items?.item.orEmpty()

                for (forecast in forecastList){

                    forecastDateTimeMap["${forecast?.fcstDate}/${forecast?.fcstTime}"]

                    if ( forecastDateTimeMap["${forecast?.fcstDate}/${forecast?.fcstTime}"] == null){
                        forecastDateTimeMap["${forecast?.fcstDate}/${forecast?.fcstTime}"] = Forecast(forecast?.fcstDate, forecast?.fcstTime)
                    }
                    forecastDateTimeMap["${forecast?.fcstDate}/${forecast?.fcstTime}"]?.apply {
                        when(forecast?.category){
                            Category.POP -> {
                                precipitation = forecast.fcstValue.toString().toInt()
                            }

                            Category.PTY -> {
                                precipitationType = transformRainType(forecast)
                            }

                            Category.SKY -> {
                                sky = transformSky(forecast)
                            }

                            Category.TMP -> {
                                temperature = forecast.fcstValue.toString().toDouble()
                            }

                            else -> {}
                        }
                    }
                }
                Log.d("test1234", forecastDateTimeMap.toString())
            }

            override fun onFailure(p0: Call<WeatherEntity>, p1: Throwable) {

            }

        })
    }
    private fun transformRainType(forecast: Item): String{
        return when(forecast.fcstValue.toString().toInt()){
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSky(forecast: Item): String{
        return when(forecast.fcstValue.toString().toInt()){
            1 -> "맑음"
            3 -> "구름 많음"
            4 -> "흐림"
            else -> ""
        }
    }
}
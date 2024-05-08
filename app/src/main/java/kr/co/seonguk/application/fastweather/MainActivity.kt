package kr.co.seonguk.application.fastweather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kr.co.seonguk.application.fastweather.databinding.ActivityMainBinding
import kr.co.seonguk.application.fastweather.databinding.ItemForecastBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permission ->
        when{
            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getMyLastLocation()
            }
            else -> {
                Toast.makeText(this, "위치 권한이 필요합니다",Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMyLastLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION))

            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {

            Thread{
                try {
                    //오래 걸리기 때문에 백그라운드 서비스를 사용하는 것이 좋다
                    val addressList = Geocoder(this, Locale.KOREA).getFromLocation(it.latitude, it.longitude, 1)

                    runOnUiThread {
                        //Thread를 사용하였기 때문에 화면관련된 Thread는 따로 작업을 해줘야함
                        binding.locationTextView.text = addressList?.get(0)?.thoroughfare.orEmpty()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }.start()

            WeatherRepository.getVillageForecast(
                it.longitude,
                it.latitude,
                successCallback = { list ->

                    val currentForecast = list.first()

                    binding.temperatureTextview.text = getString(R.string.temperature_text, currentForecast.temperature)
                    binding.skyTextview.text = currentForecast.precipitationType
                    binding.precipitationTextview.text = getString(R.string.precipitation_text, currentForecast.precipitation)


                    binding.childForecastLayout.apply {
                        list.forEachIndexed { index, forecast ->
                            if (index == 0) {return@forEachIndexed}

                            val itemView = ItemForecastBinding.inflate(layoutInflater)

                            itemView.timeTextView.text = forecast.fcstTime
                            itemView.weatherTextView.text = forecast.sky
                            itemView.temperatureTextview.text = getString(R.string.temperature_text, forecast.temperature)

                            addView(itemView.root)
                        }
                    }
                },
                failureCallback = {
                    it.printStackTrace()
                }
            )
        }
    }
}
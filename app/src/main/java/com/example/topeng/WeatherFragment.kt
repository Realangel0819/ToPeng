package com.example.topeng

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide // Glide를 사용해 이미지 로딩
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WeatherFragment : Fragment() {

    private lateinit var weatherTextView: TextView
    private lateinit var weatherIconImageView: ImageView // 아이콘 이미지를 위한 ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = BuildConfig.OPENWEATHERMAP_API_KEY // API 키 가져오기

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weatherTextView = view.findViewById(R.id.weatherTextView)
        weatherIconImageView = view.findViewById(R.id.weatherIconImageView) // 아이콘 뷰 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 권한 확인 후 위치 가져오기
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocationAndFetchWeather()
        }
    }

    private fun getCurrentLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    fetchWeatherData(latitude, longitude)
                } else {
                    Toast.makeText(requireContext(), "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "위치 요청 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getWeatherForecast(latitude, longitude, apiKey)
                }

                val cityName = response.city.name
                val country = response.city.country
                val forecast = response.list[0] // 첫 번째 예보 데이터
                val icon = forecast.weather[0].icon // icon 값 가져오기
                val iconUrl = "http://openweathermap.org/img/w/$icon.png"
                Log.d("WeatherIcon", "Icon URL: $iconUrl")
                // UI 업데이트
                weatherTextView.text = """
                    도시: $cityName, $country
                    현재 온도: ${forecast.main.temp}°C
                    날씨 상태: ${forecast.weather[0].main} (${forecast.weather[0].description})
                """.trimIndent()

                // Glide를 사용해 이미지 로드
                Glide.with(this@WeatherFragment)
                    .load(iconUrl)
                    .into(weatherIconImageView)

            } catch (e: HttpException) {
                Log.e("WeatherFetchError", "HTTP Error: ${e.code()} ${e.message()}")
                Toast.makeText(requireContext(), "HTTP 오류로 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("WeatherFetchError", "Error: ${e.message}")
                Toast.makeText(requireContext(), "오류로 데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndFetchWeather()
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

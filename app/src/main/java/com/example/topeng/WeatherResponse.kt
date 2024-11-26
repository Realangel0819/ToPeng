package com.example.topeng

data class WeatherForecastResponse(
    val list: List<Forecast>, // 날씨 예보 리스트
    val city: City            // 도시 정보
)

data class Forecast(
    val dt: Long,             // 날짜/시간
    val main: Main,           // 온도 정보
    val weather: List<Weather>
)

data class Main(
    val temp: Double,         // 현재 온도
    val temp_min: Double,     // 최저 온도
    val temp_max: Double      // 최고 온도
)

data class Weather(
    val main: String,         // 날씨 상태
    val description: String,  // 상태 설명
    val icon: String          // 날씨 아이콘 (예: "01d", "02n")
)

data class City(
    val name: String,         // 도시 이름
    val country: String       // 국가 코드
)

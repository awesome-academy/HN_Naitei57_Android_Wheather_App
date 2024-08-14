package com.example.sun.utils.base
import com.example.weather.BuildConfig

object Constant {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    val BASE_API_KEY: String
        get() = BuildConfig.BASE_API_KEY
    const val BASE_ICON_URL = "https://openweathermap.org/img/wn/"
    const val WEATHER_ENDPOINT = "weather"
    const val APPID_PARAM = "appid"
    const val UNITS_PARAM = "units"
    const val UNITS_VALUE = "metric"
    const val LAT_PARAM = "lat"
    const val LON_PARAM = "lon"
    const val QUERY_PARAM = "q"
}

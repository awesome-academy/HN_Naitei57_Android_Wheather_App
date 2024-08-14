package com.example.sun.data.model
import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("main")
    var main: Main,
    @SerializedName("weather")
    var weathers: List<Weather>,
    @SerializedName("wind")
    var wind: Wind,
    @SerializedName("clouds")
    var clouds: Clouds,
    @SerializedName("coord")
    var coord: Coord,
    @SerializedName("dt")
    var dt: Long,
    @SerializedName("name")
    var nameCity: String,
    @SerializedName("sys")
    var sys: Sys,
    var day: String = "",
    var iconWeather: String = "",
)

data class Main(
    @SerializedName("temp")
    var currentTemperature: Double,
    @SerializedName("humidity")
    var humidity: Int,
)

data class Weather(
    @SerializedName("icon")
    var iconWeather: String,
    @SerializedName("main")
    var main: String,
    @SerializedName("description")
    var description: String,
)

data class Wind(
    @SerializedName("speed")
    var windSpeed: Double,
)

data class Clouds(
    @SerializedName("all")
    var percentCloud: Int,
)

data class Coord(
    @SerializedName("lon")
    var lon: Double,
    @SerializedName("lat")
    var lat: Double,
)

data class Sys(
    @SerializedName("country")
    var country: String,
)

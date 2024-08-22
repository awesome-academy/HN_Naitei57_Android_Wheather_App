package com.sun.weather.data.repository.source.remote

import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.HourlyForcast
import com.sun.weather.data.model.HourlyForcastItem
import com.sun.weather.data.model.WeeklyForecast
import com.sun.weather.data.model.WeeklyForecastItem
import com.sun.weather.data.repository.source.WeatherDataSource
import com.sun.weather.data.repository.source.remote.fetchjson.ApiManager
import com.sun.weather.utils.Constant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemoteDataSourceImpl : WeatherDataSource.Remote {
    private val apiManager: ApiManager = ApiManager.newInstance(threadPoolSize = 4)

    override fun getCurrentWeather(
        listener: OnResultListener<CurrentWeather>,
        city: String,
    ) {
        val urlString =
            "${Constant.BASE_URL}${Constant.WEATHER_ENDPOINT}?" +
                "${Constant.QUERY_PARAM}=$city&" +
                "${Constant.APPID_PARAM}=${Constant.BASE_API_KEY}&" +
                "${Constant.UNITS_PARAM}=${Constant.UNITS_VALUE}"
        apiManager.executeApiCall(
            urlString,
            CurrentWeather::class.java,
            object : OnResultListener<CurrentWeather> {
                override fun onSuccess(data: CurrentWeather) {
                    handleCurrentWeatherSuccess(data, listener)
                }

                override fun onError(exception: Exception?) {
                    listener.onError(exception)
                }
            },
        )
    }

    override fun getCurrentLocationWeather(
        listener: OnResultListener<CurrentWeather>,
        latitude: Double,
        lontitude: Double,
    ) {
        val urlString =
            "${Constant.BASE_URL}${Constant.WEATHER_ENDPOINT}?${Constant.LAT_PARAM}=$latitude" +
                "&${Constant.LON_PARAM}=$lontitude&${Constant.APPID_PARAM}=${Constant.BASE_API_KEY}" +
                "&${Constant.UNITS_PARAM}=${Constant.UNITS_VALUE}"
        apiManager.executeApiCall(
            urlString,
            CurrentWeather::class.java,
            object : OnResultListener<CurrentWeather> {
                override fun onSuccess(data: CurrentWeather) {
                    handleCurrentWeatherSuccess(data, listener)
                }

                override fun onError(exception: Exception?) {
                    listener.onError(exception)
                }
            },
        )
    }

    private fun handleCurrentWeatherSuccess(
        data: CurrentWeather,
        listener: OnResultListener<CurrentWeather>,
    ) {
        val formattedDate = formatDate(data.dt)
        data.day = formattedDate
        data.iconWeather = getIconUrl(data).toString()
        listener.onSuccess(data)
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp * SECOND_TO_MILLIS)
        return SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(date)
    }

    private fun getIconUrl(data: CurrentWeather): String? {
        return if (data.weathers.isNotEmpty()) {
            Constant.BASE_ICON_URL + data.weathers[0].iconWeather + "@2x.png"
        } else {
            null
        }
    }

    override fun getWeeklyForecast(
        listener: OnResultListener<WeeklyForecast>,
        city: String,
    ) {
        val urlString =
            "${Constant.BASE_URL}${Constant.WEEKLY_FORECAST_ENDPOINT}?${Constant.QUERY_PARAM}=$city" +
                "&${Constant.UNITS_PARAM}=${Constant.UNITS_VALUE}&${Constant.CNT_PARAM}=${Constant.FORECAST_DAY}" +
                "&${Constant.APPID_PARAM}=${Constant.BASE_API_KEY}"
        apiManager.executeApiCall(
            urlString,
            WeeklyForecast::class.java,
            object : OnResultListener<WeeklyForecast> {
                override fun onSuccess(data: WeeklyForecast) {
                    val updatedForecastList = filterAndFormatWeeklyForecast(data)
                    val updatedData = data.copy(forecastList = updatedForecastList)
                    listener.onSuccess(updatedData)
                }

                override fun onError(exception: Exception?) {
                    listener.onError(exception)
                }
            },
        )
    }

    override fun getHourlyForecast(
        listener: OnResultListener<HourlyForcast>,
        city: String,
    ) {
        val urlString =
            "${Constant.PRO_URL}${Constant.HOURLY_FORECAST_ENDPOINT}?${Constant.QUERY_PARAM}=$city" +
                "&${Constant.UNITS_PARAM}=${Constant.UNITS_VALUE}&${Constant.CNT_PARAM}=${Constant.FORECAST_HOUR}" +
                "&${Constant.APPID_PARAM}=${Constant.BASE_API_KEY}"
        apiManager.executeApiCall(
            urlString,
            HourlyForcast::class.java,
            object : OnResultListener<HourlyForcast> {
                override fun onSuccess(data: HourlyForcast) {
                    val updatedForecastList = filterAndFormatHourlyForecast(data)
                    val updatedData = data.copy(forecastList = updatedForecastList)
                    listener.onSuccess(updatedData)
                }

                override fun onError(exception: Exception?) {
                    listener.onError(exception)
                }
            },
        )
    }

    private fun filterAndFormatWeeklyForecast(data: WeeklyForecast): List<WeeklyForecastItem> {
        val currentDate = Date()
        val dayFormat = SimpleDateFormat(DAY_ONLY_PATTERN, Locale.getDefault())
        return data.forecastList.filter { item ->
            val forecastDate = Date(item.dt * SECOND_TO_MILLIS)
            !forecastDate.before(currentDate)
        }.map { item ->
            val iconWeatherUrl = "${Constant.BASE_ICON_URL}${item.weather[0].iconWeather}@2x.png"
            val day = dayFormat.format(Date(item.dt * SECOND_TO_MILLIS))
            item.copy(
                iconWeather = iconWeatherUrl,
                day = day,
            )
        }
    }

    private fun filterAndFormatHourlyForecast(data: HourlyForcast): List<HourlyForcastItem> {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return data.forecastList.filter { item ->
            val forecastTime = dateFormat.parse(item.dtTxt)?.time ?: 0
            forecastTime > currentTime
        }.map { item ->
            val iconWeatherUrl = "${Constant.BASE_ICON_URL}${item.weather[0].iconWeather}@2x.png"
            item.copy(
                iconWeather = iconWeatherUrl,
            )
        }
    }

    companion object {
        const val SECOND_TO_MILLIS = 1000
        const val DATE_PATTERN = "EEEE yyyy-MM-dd HH:mm:ss"
        const val DAY_ONLY_PATTERN = "yyyy-MM-dd"
        private var instance: RemoteDataSourceImpl? = null

        fun getInstance() = instance ?: RemoteDataSourceImpl().also { instance = it }
    }
}

package com.example.sun.data.repository.source.remote
import com.example.sun.data.model.CurrentWeather
import com.example.sun.data.repository.source.CurrentWeatherDataSource
import com.example.sun.data.repository.source.remote.fetchjson.ApiManager
import com.example.sun.utils.base.Constant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemoteDataSourceImpl : CurrentWeatherDataSource.Remote {
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
                    val date = Date(data.dt * 1000L)
                    data.day = SimpleDateFormat("EEEE yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
                    data.iconWeather = Constant.BASE_ICON_URL + data.weathers[0].iconWeather + "@2x.png"
                    listener.onSuccess(data)
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
                    val date = Date(data.dt * SECOND_TO_MILLIS)
                    data.day = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(date)
                    if (data.weathers.isNotEmpty()) {
                        data.iconWeather = Constant.BASE_ICON_URL + data.weathers[0].iconWeather + "@2x.png"
                    }
                    listener.onSuccess(data)
                }

                override fun onError(exception: Exception?) {
                    listener.onError(exception)
                }
            },
        )
    }

    companion object {
        const val SECOND_TO_MILLIS = 1000
        const val DATE_PATTERN = "EEEE yyyy-MM-dd HH:mm:ss"
        private var instance: RemoteDataSourceImpl? = null

        fun getInstance() = instance ?: RemoteDataSourceImpl().also { instance = it }
    }
}

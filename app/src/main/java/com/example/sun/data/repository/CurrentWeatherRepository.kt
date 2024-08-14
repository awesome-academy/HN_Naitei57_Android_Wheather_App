package com.example.sun.data.repository.source

import com.example.sun.data.model.CurrentWeather
import com.example.sun.data.repository.source.remote.OnResultListener

class CurrentWeatherRepository(
    private val remote: CurrentWeatherDataSource.Remote,
    private val local: CurrentWeatherDataSource.Local,
) : CurrentWeatherDataSource.Local, CurrentWeatherDataSource.Remote {
    override fun getCurrentWeatherLocal(listener: OnResultListener<List<CurrentWeather>>) {
        local.getCurrentWeatherLocal(listener)
    }

    override fun getCurrentWeather(
        listener: OnResultListener<CurrentWeather>,
        city: String,
    ) {
        remote.getCurrentWeather(listener, city)
    }

    override fun getCurrentLocationWeather(
        listener: OnResultListener<CurrentWeather>,
        latitude: Double,
        lontitude: Double,
    ) {
        remote.getCurrentLocationWeather(listener, latitude, lontitude)
    }

    companion object {
        private var instance: CurrentWeatherRepository? = null

        fun getInstance(
            remote: CurrentWeatherDataSource.Remote,
            local: CurrentWeatherDataSource.Local,
        ) = instance ?: CurrentWeatherRepository(remote, local).also { instance = it }
    }
}

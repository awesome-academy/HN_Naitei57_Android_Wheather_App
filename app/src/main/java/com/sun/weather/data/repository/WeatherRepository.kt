package com.sun.weather.data.repository.source

import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.FavouriteLocation
import com.sun.weather.data.model.HourlyForecast
import com.sun.weather.data.model.WeeklyForecast
import com.sun.weather.data.repository.source.remote.OnResultListener

class WeatherRepository(
    private val remote: WeatherDataSource.Remote,
    private val local: WeatherDataSource.Local,
) : WeatherDataSource.Local, WeatherDataSource.Remote {
    override fun getCurrentWeatherLocal(listener: OnResultListener<CurrentWeather>) {
        local.getCurrentWeatherLocal(listener)
    }

    override fun getWeeklyForecastLocal(
        listener: OnResultListener<WeeklyForecast>,
        city: String,
    ) {
        local.getWeeklyForecastLocal(listener, city)
    }

    override fun saveWeeklyForecastLocal(weeklyForecast: WeeklyForecast) {
        local.saveWeeklyForecastLocal(weeklyForecast)
    }

    override fun getHourlyForecastLocal(
        listener: OnResultListener<HourlyForecast>,
        city: String,
    ) {
        local.getHourlyForecastLocal(listener, city)
    }

    override fun saveHourlyForecastLocal(hourlyForecast: HourlyForecast) {
        local.saveHourlyForecastLocal(hourlyForecast)
    }

    override fun insertFavoriteWeather(favouriteLocation: FavouriteLocation) {
        local.insertFavoriteWeather(favouriteLocation)
    }

    override fun getAllFavorite(): List<FavouriteLocation> {
        return local.getAllFavorite()
    }

    override fun removeFavoriteItem(id: Long) {
        local.removeFavoriteItem(id)
    }

    override fun saveCurrentWeather(currentWeather: CurrentWeather) {
        local.saveCurrentWeather(currentWeather)
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

    override fun getWeeklyForecast(
        listener: OnResultListener<WeeklyForecast>,
        city: String,
    ) {
        remote.getWeeklyForecast(listener, city)
    }

    override fun getHourlyForecast(
        listener: OnResultListener<HourlyForecast>,
        city: String,
    ) {
        remote.getHourlyForecast(listener, city)
    }

    companion object {
        private var instance: WeatherRepository? = null

        fun getInstance(
            remote: WeatherDataSource.Remote,
            local: WeatherDataSource.Local,
        ) = instance ?: WeatherRepository(remote, local).also { instance = it }
    }

    override fun getSelectedLocation(key: String): String {
        return local.getSelectedLocation(key)
    }

    override fun isFavoriteLocationExists(
        cityName: String,
        countryName: String,
    ): Boolean {
        return local.isFavoriteLocationExists(cityName, countryName)
    }
}

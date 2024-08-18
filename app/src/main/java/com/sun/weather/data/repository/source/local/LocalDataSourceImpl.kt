package com.sun.weather.data.repository.source.local

import android.content.Context
import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.FavouriteLocation
import com.sun.weather.data.model.HourlyForecast
import com.sun.weather.data.model.WeeklyForecast
import com.sun.weather.data.repository.source.WeatherDataSource
import com.sun.weather.data.repository.source.local.database.DBHelper
import com.sun.weather.data.repository.source.remote.OnResultListener
import com.sun.weather.utils.SharedPrefManager

class LocalDataSourceImpl(context: Context) : WeatherDataSource.Local {
    private val dbHelper: DBHelper = DBHelper.getInstance(context)

    override fun getSelectedLocation(key: String): String {
        return SharedPrefManager.getString(key, "") ?: ""
    }

    override fun isFavoriteLocationExists(
        cityName: String,
        countryName: String,
    ): Boolean {
        return dbHelper.isFavoriteLocationExists(cityName, countryName)
    }

    override fun getCurrentWeatherLocal(listener: OnResultListener<CurrentWeather>) {
        val currentWeather = dbHelper.getCurrentWeatherByCity(SharedPrefManager.getString("cityName", "") ?: "")
        if (currentWeather != null) {
            listener.onSuccess(currentWeather)
        } else {
            listener.onError(Exception(ERR_MSG))
        }
    }

    override fun getWeeklyForecastLocal(
        listener: OnResultListener<WeeklyForecast>,
        city: String,
    ) {
        val weeklyForecast = dbHelper.getWeeklyForecastByCity(city)
        if (weeklyForecast != null) {
            listener.onSuccess(weeklyForecast)
        } else {
            listener.onError(Exception(ERR_MSG))
        }
    }

    override fun saveWeeklyForecastLocal(weeklyForecast: WeeklyForecast) {
        dbHelper.insertWeeklyForecast(weeklyForecast)
    }

    override fun getHourlyForecastLocal(
        listener: OnResultListener<HourlyForecast>,
        city: String,
    ) {
        val hourlyForecast = dbHelper.getHourlyForecastByCity(city)
        if (hourlyForecast != null) {
            listener.onSuccess(hourlyForecast)
        } else {
            listener.onError(Exception(ERR_MSG))
        }
    }

    override fun saveHourlyForecastLocal(hourlyForecast: HourlyForecast) {
        dbHelper.insertHourlyForecast(hourlyForecast)
    }

    override fun insertFavoriteWeather(favouriteLocation: FavouriteLocation) {
        dbHelper.insertFavoriteWeather(favouriteLocation)
    }

    override fun getAllFavorite(): List<FavouriteLocation> {
        return dbHelper.getAllFavorite()
    }

    override fun removeFavoriteItem(id: Long) {
        dbHelper.removeFavoriteItem(id)
    }

    override fun saveCurrentWeather(currentWeather: CurrentWeather) {
        dbHelper.insertCurrentWeather(currentWeather)
    }

    companion object {
        const val ERR_MSG = "Không tìm thấy dữ liệu hiện tại"
        private var instance: LocalDataSourceImpl? = null

        fun getInstance(context: Context) =
            synchronized(this) {
                instance ?: LocalDataSourceImpl(context).also { instance = it }
            }
    }
}

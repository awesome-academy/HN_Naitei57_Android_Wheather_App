package com.sun.weather.data.repository.source

import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.FavouriteLocation
import com.sun.weather.data.model.HourlyForecast
import com.sun.weather.data.model.WeeklyForecast
import com.sun.weather.data.repository.source.remote.OnResultListener

interface WeatherDataSource {
    interface Local {
        fun getSelectedLocation(key: String): String

        fun isFavoriteLocationExists(
            cityName: String,
            countryName: String,
        ): Boolean

        fun getCurrentWeatherLocal(listener: OnResultListener<CurrentWeather>)

        fun saveCurrentWeather(currentWeather: CurrentWeather)

        fun getWeeklyForecastLocal(
            listener: OnResultListener<WeeklyForecast>,
            city: String,
        )

        fun saveWeeklyForecastLocal(weeklyForecast: WeeklyForecast)

        fun getHourlyForecastLocal(
            listener: OnResultListener<HourlyForecast>,
            city: String,
        )

        fun saveHourlyForecastLocal(hourlyForecast: HourlyForecast)

        fun insertFavoriteWeather(favouriteLocation: FavouriteLocation)

        fun getAllFavorite(): List<FavouriteLocation>

        fun removeFavoriteItem(id: Long)
    }

    interface Remote {
        fun getCurrentWeather(
            listener: OnResultListener<CurrentWeather>,
            city: String,
        )

        fun getCurrentLocationWeather(
            listener: OnResultListener<CurrentWeather>,
            latitude: Double,
            lontitude: Double,
        )

        fun getWeeklyForecast(
            listener: OnResultListener<WeeklyForecast>,
            city: String,
        )

        fun getHourlyForecast(
            listener: OnResultListener<HourlyForecast>,
            city: String,
        )
    }
}

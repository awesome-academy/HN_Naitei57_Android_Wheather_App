package com.sun.weather.screen.home

import com.sun.weather.data.model.CurrentWeather

interface HomeContract {
    interface Presenter {
        fun getCurrentWeather(city: String)

        fun getCurrentLocationWeather(
            latitude: Double,
            longitude: Double,
        )

        fun getSelectedLocation(key: String)

        fun saveFavoriteLocation(
            cityName: String,
            countryName: String,
        )

        fun saveCurrentWeather(currentWeather: CurrentWeather)

        fun loadDataFromLocal()
    }

    interface View {
        fun onGetCurrentWeatherSuccess(currentWeather: CurrentWeather)

        fun onGetCurrentLocationWeatherSuccess(currentWeather: CurrentWeather)

        fun onError(e: String)

        fun onGetDataFromLocalSuccess(currentWeather: CurrentWeather)

        fun onSnackBar()
    }
}

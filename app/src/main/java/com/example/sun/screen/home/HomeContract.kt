package com.example.sun.screen.home

import com.example.sun.data.model.CurrentWeather

interface HomeContract {
    interface Presenter {
        fun getCurrentWeather(city: String)

        fun getCurrentLocationWeather(
            latitude: Double,
            longitude: Double,
        )
    }

    interface View {
        fun onGetCurrentWeatherSuccess(currentWeather: CurrentWeather)

        fun onGetCurrentLocationWeatherSuccess(currentWeather: CurrentWeather)

        fun onError(e: String)
    }
}

package com.sun.weather.screen.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.FavouriteLocation
import com.sun.weather.data.repository.source.WeatherRepository
import com.sun.weather.data.repository.source.remote.OnResultListener
import com.sun.weather.utils.SharedPrefManager

class HomePresenter(
    private val weatherRepository: WeatherRepository,
) : HomeContract.Presenter {
    private var view: HomeContract.View? = null
    private val handler = Handler(Looper.getMainLooper())

    fun setView(view: HomeContract.View) {
        this.view = view
    }

    override fun getCurrentWeather(city: String) {
        weatherRepository.getCurrentWeather(
            object : OnResultListener<CurrentWeather> {
                override fun onSuccess(data: CurrentWeather) {
                    handler.post {
                        weatherRepository.saveCurrentWeather(data)
                        view?.onGetCurrentWeatherSuccess(data)
                    }
                }

                override fun onError(exception: Exception?) {
                    handler.post {
                        view?.onError(exception?.message ?: "Đã xảy ra lỗi")
                    }
                }
            },
            city,
        )
    }

    override fun getCurrentLocationWeather(
        latitude: Double,
        longitude: Double,
    ) {
        weatherRepository.getCurrentLocationWeather(
            object : OnResultListener<CurrentWeather> {
                override fun onSuccess(data: CurrentWeather) {
                    handler.post {
                        weatherRepository.saveCurrentWeather(data)
                        view?.onGetCurrentLocationWeatherSuccess(data)
                    }
                }

                override fun onError(exception: Exception?) {
                    handler.post {
                        view?.onError(exception.toString())
                    }
                }
            },
            latitude,
            longitude,
        )
    }

    override fun getSelectedLocation(key: String) {
        val city = weatherRepository.getSelectedLocation(key)
        getCurrentWeather(city)
    }

    override fun saveFavoriteLocation(
        cityName: String,
        countryName: String,
    ) {
        if (weatherRepository.isFavoriteLocationExists(cityName, countryName)) {
            view?.onSnackBar()
        } else {
            val favouriteLocation =
                FavouriteLocation(
                    cityName = cityName,
                    countryName = countryName,
                )
            weatherRepository.insertFavoriteWeather(favouriteLocation)
        }
    }

    override fun saveCurrentWeather(currentWeather: CurrentWeather) {
        with(SharedPrefManager) {
            putString("cityName", currentWeather.nameCity)
            putString("countryName", currentWeather.sys.country)
            putString("description", currentWeather.weathers[0].description)
            putFloat("temperature", currentWeather.main.currentTemperature.toFloat())
        }
    }

    override fun loadDataFromLocal() {
        weatherRepository.getCurrentWeatherLocal(
            object : OnResultListener<CurrentWeather> {
                override fun onSuccess(data: CurrentWeather) {
                    handler.post {
                        Log.d("LCD", "Loaded data from local: $data")
                        view?.onGetCurrentWeatherSuccess(data)
                    }
                }

                override fun onError(exception: Exception?) {
                    handler.post {
                        view?.onError(exception.toString())
                    }
                }
            },
        )
    }
}

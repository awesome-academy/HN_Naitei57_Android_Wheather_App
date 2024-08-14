package com.example.sun.screen.home

import android.os.Handler
import android.os.Looper
import com.example.sun.data.model.CurrentWeather
import com.example.sun.data.repository.source.CurrentWeatherRepository
import com.example.sun.data.repository.source.remote.OnResultListener
import java.lang.Exception

class HomePresenter(
    private val weatherRepository: CurrentWeatherRepository,
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
}

package com.sun.weather.screen.home

import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.bumptech.glide.Glide
import com.sun.weather.R
import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.repository.source.WeatherRepository
import com.sun.weather.data.repository.source.local.LocalDataSourceImpl
import com.sun.weather.data.repository.source.remote.RemoteDataSourceImpl
import com.sun.weather.databinding.FragmentHomeBinding
import com.sun.weather.screen.detail.DetailFragment
import com.sun.weather.screen.favourite.FavouriteFragment
import com.sun.weather.screen.search.SearchFragment
import com.sun.weather.utils.NetworkHelper
import com.sun.weather.utils.RequestLocation.requestLocationAndFetchWeather
import com.sun.weather.utils.base.BaseFragment
import com.sun.weather.utils.ext.replaceFragment
import kotlin.math.roundToInt

class HomeFragment : BaseFragment<FragmentHomeBinding>(), HomeContract.View {
    private lateinit var currentWeather: CurrentWeather
    private var cityName: String? = null
    private var isNetworkAvailable = false

    override fun inflateViewBinding(inflater: LayoutInflater): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater)
    }

    override fun initData() {
        val repository =
            context?.let { context ->
                WeatherRepository.getInstance(
                    RemoteDataSourceImpl.getInstance(),
                    LocalDataSourceImpl.getInstance(context),
                )
            }
        homePresenter = repository?.let { HomePresenter(it) }
        homePresenter?.setView(this)
        isNetworkAvailable = NetworkHelper.isNetworkAvailable(requireContext())
        handleWeatherData()
        viewBinding.icLocation.setOnClickListener {
            handleWeatherData()
        }
        viewBinding.tvLocation.setOnClickListener {
            replaceFragment(R.id.fragment_container, SearchFragment.newInstance(), true)
        }
        viewBinding.icArrowDown.setOnClickListener {
            homePresenter?.getSelectedLocation(SELECTED_LOCATION)
        }
        viewBinding.btnAddFavourite.setOnClickListener {
            val countryName = currentWeather.sys.country
            homePresenter?.saveFavoriteLocation(cityName!!, countryName)
            replaceFragment(R.id.fragment_container, FavouriteFragment.newInstance(), true)
        }
        viewBinding.constraintLayout.setOnClickListener {
            replaceFragment(R.id.fragment_container, DetailFragment.newInstance(cityName!!), true)
        }
    }

    private fun handleWeatherData() {
        if (isNetworkAvailable) {
            requestLocationAndFetchWeather(requireContext(), requireActivity(), homePresenter!!)
        } else {
            homePresenter?.loadDataFromLocal()
        }
    }

    private fun updateUIWithCurrentWeather(currentWeather: CurrentWeather) {
        viewBinding.tvLocation.text =
            getString(R.string.city_name, currentWeather.nameCity, currentWeather.sys.country)
        cityName = currentWeather.nameCity
        viewBinding.tvCurrentDay.text = getString(R.string.today) + currentWeather.day
        viewBinding.tvCurrentTemperature.text = currentWeather.main.currentTemperature.roundToInt().toString() + "°C"
        if (currentWeather.weathers.isNotEmpty()) {
            viewBinding.tvCurrentText.text = currentWeather.weathers[0].description
        }
        viewBinding.tvCurrentPercentCloud.text = currentWeather.wind.windSpeed.toString()
        viewBinding.tvCurrentHumidity.text = currentWeather.main.humidity.toString()
        viewBinding.tvCurrentPercentCloud1.text = currentWeather.clouds.percentCloud.toString()
        Glide.with(this)
            .load(currentWeather.iconWeather)
            .into(viewBinding.ivCurrentWeather)
    }

    override fun initView() {
    }

    override fun onGetCurrentWeatherSuccess(currentWeather: CurrentWeather) {
        cityName = currentWeather.nameCity
        this.currentWeather = currentWeather
        updateUIWithCurrentWeather(currentWeather)
    }

    override fun onGetCurrentLocationWeatherSuccess(currentWeather: CurrentWeather) {
        cityName = currentWeather.nameCity
        this.currentWeather = currentWeather
        updateUIWithCurrentWeather(currentWeather)
        homePresenter?.saveCurrentWeather(currentWeather)
    }

    override fun onError(e: String) {
        Log.d(MY_TAG, "onError: $e")
    }

    override fun onGetDataFromLocalSuccess(currentWeather: CurrentWeather) {
        updateUIWithCurrentWeather(currentWeather)
    }

    override fun onSnackBar() {
        Toast.makeText(requireContext(), getString(R.string.already_favorite), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter = null
    }

    companion object {
        var homePresenter: HomePresenter? = null
        const val MY_TAG = "HomeFragment"
        const val SELECTED_LOCATION = "selected_location"

        fun newInstance() = HomeFragment()
    }
}

package com.example.sun.screen.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.sun.data.model.CurrentWeather
import com.example.sun.data.repository.source.CurrentWeatherRepository
import com.example.sun.data.repository.source.local.LocalDataSourceImpl
import com.example.sun.data.repository.source.remote.RemoteDataSourceImpl
import com.example.sun.utils.base.BaseFragment
import com.example.weather.databinding.FragmentHomeBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class HomeFragment : BaseFragment<FragmentHomeBinding>(), HomeContract.View {
    private var homePresenter: HomePresenter? = null
    private lateinit var currentWeather: CurrentWeather

    override fun inflateViewBinding(inflater: LayoutInflater): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater)
    }

    override fun initData() {
        homePresenter =
            HomePresenter(
                CurrentWeatherRepository.getInstance(
                    RemoteDataSourceImpl.getInstance(),
                    LocalDataSourceImpl.getInstance(),
                ),
            )
        homePresenter?.setView(this)
        viewBinding.icLocation.setOnClickListener {
            requestLocationAndFetchWeather()
        }
    }

    private fun requestLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                REQUEST_CODE,
            )
        } else {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
            locationTask.addOnSuccessListener { location ->
                if (location != null) {
                    homePresenter?.getCurrentLocationWeather(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUIWithCurrentWeather(currentWeather: CurrentWeather) {
        viewBinding.tvLocation.text = "${currentWeather.nameCity}, ${currentWeather.sys.country}"
        viewBinding.tvCurrentDay.text = currentWeather.day
        viewBinding.tvCurrentTemperature.text = currentWeather.main.currentTemperature.toString()
        if (!currentWeather.weathers.isEmpty()) {
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
    }

    override fun onGetCurrentLocationWeatherSuccess(currentWeather: CurrentWeather) {
        this.currentWeather = currentWeather
        updateUIWithCurrentWeather(currentWeather)
    }

    override fun onError(e: String) {
        Log.d(MY_TAG, "onError: $e")
        Toast.makeText(context, "Lỗi: $e", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter = null
    }

    companion object {
        const val MY_TAG = "HomeFragment"
        const val REQUEST_CODE = 1000

        fun newInstance() = HomeFragment()
    }
}

package com.sun.weather.utils
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.sun.weather.BuildConfig
import com.sun.weather.screen.home.HomePresenter

object Constant {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val PRO_URL = "https://pro.openweathermap.org/data/2.5/"
    val BASE_API_KEY: String
        get() = BuildConfig.BASE_API_KEY
    const val BASE_ICON_URL = "https://openweathermap.org/img/wn/"
    const val WEATHER_ENDPOINT = "weather"
    const val WEEKLY_FORECAST_ENDPOINT = "forecast/daily"
    const val HOURLY_FORECAST_ENDPOINT = "forecast/hourly"
    const val APPID_PARAM = "appid"
    const val UNITS_PARAM = "units"
    const val UNITS_VALUE = "metric"
    const val LAT_PARAM = "lat"
    const val LON_PARAM = "lon"
    const val QUERY_PARAM = "q"
    const val CNT_PARAM = "cnt"
    const val LANGUAGE_PARAM = "lang"
    const val FORECAST_DAY = 7
    const val FORECAST_HOUR = 24
    const val LANGUAGE_CODE_VIETNAMESE = "vi"
    const val LANGUAGE_CODE_ENGLISH = "en"
}

object SharedPrefManager {
    private const val SHARE_PREFERENCES_NAME = "SHARE_PREFERENCES"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getString(
        key: String,
        defaultValue: String?,
    ): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putString(
        key: String,
        value: String,
    ) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getFloat(
        key: String,
        defaultValue: Float,
    ): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putFloat(
        key: String,
        value: Float,
    ) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun getInt(
        key: String,
        defaultValue: Int,
    ): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putInt(
        key: String,
        value: Int,
    ) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}

object RequestLocation {
    private const val REQUEST_CODE = 1000
    private const val LATITUDE = "latitude"
    private const val LONGITUDE = "longitude"

    fun requestLocationAndFetchWeather(
        context: Context,
        activity: Activity,
        homePresenter: HomePresenter,
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                REQUEST_CODE,
            )
        } else {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val locationTask: Task<Location> = fusedLocationProviderClient.lastLocation
            locationTask.addOnSuccessListener { location ->
                if (location != null) {
                    SharedPrefManager.putString(LATITUDE, location.latitude.toString())
                    SharedPrefManager.putString(LONGITUDE, location.longitude.toString())
                    homePresenter.getCurrentLocationWeather(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

object NetworkHelper {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

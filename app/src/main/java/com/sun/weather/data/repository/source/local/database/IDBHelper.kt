package com.sun.weather.data.repository.source.local.database

import com.sun.weather.data.model.FavouriteLocation

interface IDBHelper {
    fun insertFavoriteWeather(favouriteLocation: FavouriteLocation): Long

    fun getAllFavorite(): List<FavouriteLocation>

    fun removeFavoriteItem(id: Long): Int

    fun isFavoriteLocationExists(
        cityName: String,
        countryName: String,
    ): Boolean
}

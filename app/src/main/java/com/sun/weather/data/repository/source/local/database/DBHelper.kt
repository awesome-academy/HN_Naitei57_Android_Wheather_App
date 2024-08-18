package com.sun.weather.data.repository.source.local.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sun.weather.data.model.City
import com.sun.weather.data.model.Clouds
import com.sun.weather.data.model.Coord
import com.sun.weather.data.model.CurrentWeather
import com.sun.weather.data.model.FavouriteLocation
import com.sun.weather.data.model.HourlyForecast
import com.sun.weather.data.model.HourlyForecastItem
import com.sun.weather.data.model.Main
import com.sun.weather.data.model.Sys
import com.sun.weather.data.model.Temp
import com.sun.weather.data.model.Weather
import com.sun.weather.data.model.WeeklyForecast
import com.sun.weather.data.model.WeeklyForecastItem
import com.sun.weather.data.model.Wind

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IDBHelper {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.apply {
            execSQL(CREATE_TABLE_FAVORITES)
            execSQL(CREATE_TABLE_CURRENT_WEATHER)
            execSQL(CREATE_TABLE_HOURLY_FORECAST)
            execSQL(CREATE_TABLE_HOURLY_FORECAST_ITEM)
            execSQL(CREATE_TABLE_WEEKLY_FORECAST)
            execSQL(CREATE_TABLE_WEEKLY_FORECAST_ITEM)
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db?.apply {
            execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
            execSQL("DROP TABLE IF EXISTS $TABLE_CURRENT_WEATHER")
            execSQL("DROP TABLE IF EXISTS $TABLE_HOURLY_FORECAST")
            execSQL("DROP TABLE IF EXISTS $TABLE_HOURLY_FORECAST_ITEM")
            execSQL("DROP TABLE IF EXISTS $TABLE_WEEKLY_FORECAST")
            execSQL("DROP TABLE IF EXISTS $TABLE_WEEKLY_FORECAST_ITEM")
            onCreate(db)
        }
    }

    override fun insertFavoriteWeather(favouriteLocation: FavouriteLocation): Long {
        val db = writableDatabase
        val contentValues =
            ContentValues().apply {
                put(COLUMN_CITY_NAME, favouriteLocation.cityName)
                put(COLUMN_COUNTRY_NAME, favouriteLocation.countryName)
            }
        return db.insert(TABLE_FAVORITES, null, contentValues)
    }

    override fun getAllFavorite(): List<FavouriteLocation> {
        val db = readableDatabase
        val cursor = db.query(TABLE_FAVORITES, null, null, null, null, null, null)
        val favoriteList = mutableListOf<FavouriteLocation>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val cityName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME))
                val countryName =
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY_NAME))
                favoriteList.add(FavouriteLocation(id, cityName, countryName))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return favoriteList
    }

    override fun removeFavoriteItem(id: Long): Int {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        return db.delete(TABLE_FAVORITES, whereClause, whereArgs)
    }

    override fun isFavoriteLocationExists(
        cityName: String,
        countryName: String,
    ): Boolean {
        val db = readableDatabase
        val cursor =
            db.query(
                TABLE_FAVORITES,
                arrayOf(COLUMN_CITY_NAME),
                "$COLUMN_CITY_NAME = ? AND $COLUMN_COUNTRY_NAME = ?",
                arrayOf(cityName, countryName),
                null,
                null,
                null,
            )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun insertCurrentWeather(currentWeather: CurrentWeather): Long {
        val db = writableDatabase
        val contentValues =
            ContentValues().apply {
                put(COLUMN_CITY_NAME, currentWeather.nameCity)
                put(COLUMN_COUNTRY_NAME, currentWeather.sys.country)
                put(COLUMN_TEMP, currentWeather.main.currentTemperature)
                put(COLUMN_HUMIDITY, currentWeather.main.humidity)
                put(COLUMN_WEATHER_DESCRIPTION, currentWeather.weathers[0].description)
                put(COLUMN_WIND_SPEED, currentWeather.wind.windSpeed)
                put(COLUMN_CLOUD_PERCENT, currentWeather.clouds.percentCloud)
                put(COLUMN_DAY, currentWeather.day)
                put(COLUMN_ICON_WEATHER, currentWeather.iconWeather)
            }
        return db.insert(TABLE_CURRENT_WEATHER, null, contentValues)
    }

    fun getCurrentWeatherByCity(cityName: String): CurrentWeather? {
        val db = readableDatabase
        val selection = "$COLUMN_CITY_NAME = ?"
        val selectionArgs = arrayOf(cityName)
        val cursor =
            db.query(
                TABLE_CURRENT_WEATHER,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null,
            )

        var currentWeather: CurrentWeather? = null

        if (cursor.moveToFirst()) {
            val main =
                Main(
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TEMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HUMIDITY)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TEMP_MIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TEMP_MAX)),
                )

            val weather =
                Weather(
                    "",
                    "",
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEATHER_DESCRIPTION)),
                )

            val wind =
                Wind(
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WIND_SPEED)),
                )

            val clouds =
                Clouds(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLOUD_PERCENT)),
                )

            val sys =
                Sys(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY_NAME)),
                )

            currentWeather =
                CurrentWeather(
                    main = main,
                    weathers = listOf(weather),
                    wind = wind,
                    clouds = clouds,
                    coord = Coord(0.0, 0.0),
                    dt = 0,
                    nameCity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME)),
                    sys = sys,
                    day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                    iconWeather = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_WEATHER)),
                )
        }

        cursor.close()
        return currentWeather
    }

    fun insertHourlyForecast(hourlyForecast: HourlyForecast): Long {
        val db = writableDatabase
        val contentValues =
            ContentValues().apply {
                put(COLUMN_CITY_ID, hourlyForecast.city.id)
                put(COLUMN_CITY_NAME, hourlyForecast.city.name)
                put(COLUMN_COUNTRY_NAME, hourlyForecast.city.country)
                put(COLUMN_CNT, hourlyForecast.cnt)
            }
        val forecastId = db.insert(TABLE_HOURLY_FORECAST, null, contentValues)
        hourlyForecast.forecastList.forEach { item ->
            val itemValues =
                ContentValues().apply {
                    put(COLUMN_FORECAST_ID, forecastId)
                    put(COLUMN_DT, item.dt)
                    put(COLUMN_TEMP, item.main.currentTemperature)
                    put(COLUMN_HUMIDITY, item.main.humidity)
                    put(COLUMN_WEATHER_DESCRIPTION, item.weather[0].description)
                    put(COLUMN_WIND_SPEED, item.wind.windSpeed)
                    put(COLUMN_CLOUD_PERCENT, item.clouds.percentCloud)
                    put(COLUMN_DT_TXT, item.dtTxt)
                    put(COLUMN_ICON_WEATHER, item.iconWeather)
                }
            db.insert(TABLE_HOURLY_FORECAST_ITEM, null, itemValues)
        }
        return forecastId
    }

    fun getHourlyForecastByCity(cityName: String): HourlyForecast? {
        val db = readableDatabase
        val selection = "$COLUMN_CITY_NAME = ?"
        val selectionArgs = arrayOf(cityName)
        val cursor =
            db.query(
                TABLE_HOURLY_FORECAST,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null,
            )

        var hourlyForecast: HourlyForecast? = null
        if (cursor.moveToFirst()) {
            val city =
                City(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CITY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME)),
                    Coord(0.0, 0.0),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY_NAME)),
                )
            val cnt = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CNT))
            val forecastItems = mutableListOf<HourlyForecastItem>()
            val forecastCursor =
                db.query(
                    TABLE_HOURLY_FORECAST_ITEM,
                    null,
                    "$COLUMN_FORECAST_ID = ?",
                    arrayOf(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString()),
                    null,
                    null,
                    null,
                )

            if (forecastCursor.moveToFirst()) {
                do {
                    val main =
                        Main(
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP)),
                            forecastCursor.getInt(forecastCursor.getColumnIndexOrThrow(COLUMN_HUMIDITY)),
                            0.0,
                            0.0,
                        )

                    val weather =
                        Weather(
                            "",
                            "",
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_WEATHER_DESCRIPTION)),
                        )

                    val wind = Wind(forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_WIND_SPEED)))

                    val clouds = Clouds(forecastCursor.getInt(forecastCursor.getColumnIndexOrThrow(COLUMN_CLOUD_PERCENT)))

                    val forecastItem =
                        HourlyForecastItem(
                            forecastCursor.getLong(forecastCursor.getColumnIndexOrThrow(COLUMN_DT)),
                            main,
                            listOf(weather),
                            clouds,
                            wind,
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_DT_TXT)),
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_ICON_WEATHER)),
                        )
                    forecastItems.add(forecastItem)
                } while (forecastCursor.moveToNext())
            }
            forecastCursor.close()

            hourlyForecast = HourlyForecast(cnt, forecastItems, city)
        }
        cursor.close()

        return hourlyForecast
    }

    fun insertWeeklyForecast(weeklyForecast: WeeklyForecast): Long {
        val db = writableDatabase
        val contentValues =
            ContentValues().apply {
                put(COLUMN_CITY_ID, weeklyForecast.city.id)
                put(COLUMN_CITY_NAME, weeklyForecast.city.name)
                put(COLUMN_COUNTRY_NAME, weeklyForecast.city.country)
                put(COLUMN_CNT, weeklyForecast.cnt)
            }
        val forecastId = db.insert(TABLE_WEEKLY_FORECAST, null, contentValues)
        weeklyForecast.forecastList.forEach { item ->
            val itemValues =
                ContentValues().apply {
                    put(COLUMN_FORECAST_ID, forecastId)
                    put(COLUMN_DAY, item.day)
                    put(COLUMN_TEMP_DAY, item.temp.day)
                    put(COLUMN_TEMP_MIN, item.temp.min)
                    put(COLUMN_TEMP_MAX, item.temp.max)
                    put(COLUMN_TEMP_NIGHT, item.temp.night)
                    put(COLUMN_TEMP_EVE, item.temp.eve)
                    put(COLUMN_TEMP_MORN, item.temp.morn)
                    put(COLUMN_HUMIDITY, item.humidity)
                    put(COLUMN_WEATHER_DESCRIPTION, item.weather[0].description)
                    put(COLUMN_WIND_SPEED, item.speed)
                    put(COLUMN_CLOUD_PERCENT, item.clouds)
                    put(COLUMN_ICON_WEATHER, item.iconWeather)
                }
            db.insert(TABLE_WEEKLY_FORECAST_ITEM, null, itemValues)
        }
        return forecastId
    }

    fun getWeeklyForecastByCity(cityName: String): WeeklyForecast? {
        val db = readableDatabase
        val selection = "$COLUMN_CITY_NAME = ?"
        val selectionArgs = arrayOf(cityName)
        val cursor =
            db.query(
                TABLE_WEEKLY_FORECAST,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null,
            )

        var weeklyForecast: WeeklyForecast? = null
        if (cursor.moveToFirst()) {
            val city =
                City(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CITY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME)),
                    Coord(0.0, 0.0),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY_NAME)),
                )
            val cnt = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CNT))
            val forecastItems = mutableListOf<WeeklyForecastItem>()
            val forecastCursor =
                db.query(
                    TABLE_WEEKLY_FORECAST_ITEM,
                    null,
                    "$COLUMN_FORECAST_ID = ?",
                    arrayOf(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString()),
                    null,
                    null,
                    null,
                )
            if (forecastCursor.moveToFirst()) {
                do {
                    val temp =
                        Temp(
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_DAY)),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_MIN)),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_MAX)),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_NIGHT)),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_EVE)),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_TEMP_MORN)),
                        )
                    val weather =
                        Weather(
                            "",
                            "",
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_WEATHER_DESCRIPTION)),
                        )
                    val forecastItem =
                        WeeklyForecastItem(
                            0,
                            temp,
                            forecastCursor.getInt(forecastCursor.getColumnIndexOrThrow(COLUMN_HUMIDITY)),
                            listOf(weather),
                            forecastCursor.getDouble(forecastCursor.getColumnIndexOrThrow(COLUMN_WIND_SPEED)),
                            forecastCursor.getInt(forecastCursor.getColumnIndexOrThrow(COLUMN_CLOUD_PERCENT)),
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_DAY)),
                            forecastCursor.getString(forecastCursor.getColumnIndexOrThrow(COLUMN_ICON_WEATHER)),
                        )
                    forecastItems.add(forecastItem)
                } while (forecastCursor.moveToNext())
            }
            forecastCursor.close()
            weeklyForecast = WeeklyForecast(city, cnt, forecastItems)
        }
        cursor.close()
        return weeklyForecast
    }

    companion object {
        private const val TAG = "DBHelper"
        private const val DATABASE_NAME = "weather.db"
        private const val DATABASE_VERSION = 5
        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CITY_NAME = "cityName"
        private const val COLUMN_COUNTRY_NAME = "countryName"
        private const val CREATE_TABLE_FAVORITES = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CITY_NAME TEXT,
                $COLUMN_COUNTRY_NAME TEXT
            )
        """

        private const val TABLE_CURRENT_WEATHER = "current_weather"
        private const val COLUMN_TEMP = "temperature"
        private const val COLUMN_TEMP_MIN = "temp_min"
        private const val COLUMN_TEMP_MAX = "temp_max"
        private const val COLUMN_HUMIDITY = "humidity"
        private const val COLUMN_WEATHER_DESCRIPTION = "weather_description"
        private const val COLUMN_WIND_SPEED = "wind_speed"
        private const val COLUMN_CLOUD_PERCENT = "cloud_percent"
        private const val COLUMN_DAY = "day"
        private const val COLUMN_ICON_WEATHER = "icon_weather"

        private const val CREATE_TABLE_CURRENT_WEATHER = """
            CREATE TABLE $TABLE_CURRENT_WEATHER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CITY_NAME TEXT,
                $COLUMN_COUNTRY_NAME TEXT,
                $COLUMN_TEMP REAL,
                $COLUMN_HUMIDITY INTEGER,
                $COLUMN_TEMP_MIN REAL,
                $COLUMN_TEMP_MAX REAL,
                $COLUMN_WEATHER_DESCRIPTION TEXT,
                $COLUMN_WIND_SPEED REAL,
                $COLUMN_CLOUD_PERCENT INTEGER,
                $COLUMN_DAY TEXT,
                $COLUMN_ICON_WEATHER TEXT
    )
"""

        // Bảng Hourly Forecast
        private const val TABLE_HOURLY_FORECAST = "hourly_forecast"
        private const val COLUMN_CITY_ID = "city_id"
        private const val COLUMN_CNT = "cnt"
        private const val CREATE_TABLE_HOURLY_FORECAST = """
            CREATE TABLE $TABLE_HOURLY_FORECAST (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CITY_ID INTEGER,
                $COLUMN_CITY_NAME TEXT,
                $COLUMN_COUNTRY_NAME TEXT,
                $COLUMN_CNT INTEGER
            )
        """

        private const val TABLE_HOURLY_FORECAST_ITEM = "hourly_forecast_item"
        private const val COLUMN_FORECAST_ID = "forecast_id"
        private const val COLUMN_DT = "dt"
        private const val COLUMN_DT_TXT = "dt_txt"
        private const val CREATE_TABLE_HOURLY_FORECAST_ITEM = """
            CREATE TABLE $TABLE_HOURLY_FORECAST_ITEM (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FORECAST_ID INTEGER,
                $COLUMN_DT LONG,
                $COLUMN_TEMP REAL,
                $COLUMN_HUMIDITY INTEGER,
                $COLUMN_WEATHER_DESCRIPTION TEXT,
                $COLUMN_WIND_SPEED REAL,
                $COLUMN_CLOUD_PERCENT INTEGER,
                $COLUMN_DT_TXT TEXT,
                $COLUMN_ICON_WEATHER TEXT,
                FOREIGN KEY($COLUMN_FORECAST_ID) REFERENCES $TABLE_HOURLY_FORECAST($COLUMN_ID)
            )
        """

        // Bảng Weekly Forecast
        private const val TABLE_WEEKLY_FORECAST = "weekly_forecast"
        private const val CREATE_TABLE_WEEKLY_FORECAST = """
            CREATE TABLE $TABLE_WEEKLY_FORECAST (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CITY_ID INTEGER,
                $COLUMN_CITY_NAME TEXT,
                $COLUMN_COUNTRY_NAME TEXT,
                $COLUMN_CNT INTEGER
            )
        """
        private const val TABLE_WEEKLY_FORECAST_ITEM = "weekly_forecast_item"
        private const val COLUMN_TEMP_DAY = "temp_day"
        private const val COLUMN_TEMP_NIGHT = "temp_night"
        private const val COLUMN_TEMP_EVE = "temp_eve"
        private const val COLUMN_TEMP_MORN = "temp_morn"
        private const val CREATE_TABLE_WEEKLY_FORECAST_ITEM = """
            CREATE TABLE $TABLE_WEEKLY_FORECAST_ITEM (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FORECAST_ID INTEGER,
                $COLUMN_DAY TEXT,
                $COLUMN_TEMP_DAY REAL,
                $COLUMN_TEMP_MIN REAL,
                $COLUMN_TEMP_MAX REAL,
                $COLUMN_TEMP_NIGHT REAL,
                $COLUMN_TEMP_EVE REAL,
                $COLUMN_TEMP_MORN REAL,
                $COLUMN_HUMIDITY INTEGER,
                $COLUMN_WEATHER_DESCRIPTION TEXT,
                $COLUMN_WIND_SPEED REAL,
                $COLUMN_CLOUD_PERCENT INTEGER,
                $COLUMN_ICON_WEATHER TEXT,
                FOREIGN KEY($COLUMN_FORECAST_ID) REFERENCES $TABLE_WEEKLY_FORECAST($COLUMN_ID)
            )
        """

        private var instance: DBHelper? = null

        fun getInstance(context: Context): DBHelper {
            if (instance == null) {
                synchronized(this) {
                    instance = DBHelper(context)
                }
            }
            return instance!!
        }
    }
}

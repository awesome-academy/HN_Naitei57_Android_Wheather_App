package com.example.sun.data.repository.source.remote.fetchjson
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sun.data.repository.source.remote.OnResultListener
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ApiManager private constructor(private val threadPoolSize: Int) {
    private lateinit var mExecutor: ExecutorService
    private val mHandler = Handler(Looper.getMainLooper())

    private fun getExecutor(): ExecutorService {
        if (!::mExecutor.isInitialized) {
            mExecutor = Executors.newFixedThreadPool(threadPoolSize)
        }
        return mExecutor
    }

    fun <T> executeApiCall(
        urlString: String,
        responseType: Class<T>,
        listener: OnResultListener<T>,
    ) {
        getExecutor().submit {
            try {
                val responseJson = getJsonStringFromUrl(urlString)
                val gson = Gson()
                val weather = gson.fromJson(responseJson, responseType)
                mHandler.post {
                    listener.onSuccess(weather)
                }
            } catch (e: Exception) {
                mHandler.post {
                    listener.onError(e)
                }
            }
        }
    }

    private fun getJsonStringFromUrl(urlString: String): String {
        val url = URL(urlString)
        val httpURLConnection = url.openConnection() as? HttpURLConnection
        return try {
            httpURLConnection?.run {
                connectTimeout = TIME_OUT
                readTimeout = TIME_OUT
                requestMethod = METHOD_GET
                doOutput = true
                connect()
            }
            if (httpURLConnection?.responseCode == HttpURLConnection.HTTP_OK) {
                val bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                bufferedReader.close()
                stringBuilder.toString()
            } else {
                Log.d(LOG_TAG, "HTTP error code: ${httpURLConnection?.responseCode}")
                throw Exception("City not found: $urlString")
            }
        } catch (e: MalformedURLException) {
            Log.d(LOG_TAG, "MalformedURLException: ${e.message}")
            throw e
        } catch (e: IOException) {
            Log.d(LOG_TAG, "IOException: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Exception: ${e.message}")
            throw e
        } finally {
            httpURLConnection?.disconnect()
        }
    }

    companion object {
        const val LOG_TAG = "ApiManager"
        const val TIME_OUT = 15000
        const val METHOD_GET = "GET"

        @Volatile
        private var instance: ApiManager? = null

        fun newInstance(threadPoolSize: Int): ApiManager {
            return instance ?: synchronized(this) {
                instance ?: ApiManager(threadPoolSize).also { instance = it }
            }
        }
    }
}

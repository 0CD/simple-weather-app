package com.example.weatherapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class WeatherData(
    val name: String,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val sys: Sys
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int
)

data class ForecastData(
    val list: List<ForecastList>,
    val cnt: Int
)

data class ForecastList(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Sys(
    val sunrise: Int,
    val sunset: Int
)

const val BASE_URL = "https://api.openweathermap.org"

// Retrofit service to fetch data
interface APIService {
    @GET("/data/2.5/weather")
    suspend fun fetchData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): WeatherData

    @GET("/data/2.5/weather")
    suspend fun fetchDataQuery(
        @Query("q") query: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): WeatherData

    @GET("/data/2.5/forecast")
    suspend fun fetchForecastData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String,
        @Query("cnt") count: String,
        @Query("appid") apiKey: String
    ): ForecastData

    @GET("/data/2.5/forecast")
    suspend fun fetchForecastDataQuery(
        @Query("q") query: String,
        @Query("units") units: String,
        @Query("cnt") count: String,
        @Query("appid") apiKey: String
    ): ForecastData

    companion object {
        private var apiService: APIService? = null
        fun getInstance(): APIService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }
            return apiService!!
        }
    }
}

// ViewModel to fetch data
class WeatherViewModel : ViewModel() {
    var weatherItem: WeatherData? by mutableStateOf(null)
    var forecastItem: ForecastData? by mutableStateOf(null)
    var errorMessage: String by mutableStateOf("")

    var effectLaunched: Boolean by mutableStateOf(false)

    // Function to fetch data
    // This function is called when the app is launched
    // It fetches the weather data for the current location
    // The latitude and longitude of the current location are passed as parameters
    fun getWeatherData(lat: String, lon: String) {
        viewModelScope.launch {
            val apiService = APIService.getInstance()
            try {
                weatherItem = apiService.fetchData(lat, lon, "metric", "<YOUR_API_KEY>")
                forecastItem = apiService.fetchForecastData(lat, lon, "metric", "8", "<YOUR_API_KEY>")
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            }
        }
    }

    // Function to fetch data
    // It fetches the weather data for the location specified in the query
    // The query is the name of the location
    fun getWeatherDataQuery(query: String) {
        viewModelScope.launch {
            val apiService = APIService.getInstance()
            try {
                weatherItem = apiService.fetchDataQuery(query, "metric", "<YOUR_API_KEY>")
                forecastItem = apiService.fetchForecastDataQuery(query, "metric", "8", "<YOUR_API_KEY>")
            } catch (e: Exception) {
                errorMessage = e.message.toString()
            }
        }
    }
}
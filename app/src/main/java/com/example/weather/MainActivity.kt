package com.example.weather
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {



    data class WeatherData(
        @SerializedName("sys") val sys: Sys,
        @SerializedName("name") val name: String,
        @SerializedName("weather") val weather: List<Weather>,
        @SerializedName("wind") val wind: Wind
    )

    data class Sys(
        @SerializedName("sunrise") val sunrise: Long,
        @SerializedName("sunset") val sunset: Long
    )

    data class Weather(
        @SerializedName("description") val description: String
    )

    data class Wind(
        @SerializedName("speed") val speed: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonRefresh = findViewById<Button>(R.id.buttonRefresh)
        buttonRefresh.setOnClickListener {
            onClick(it)
        }
    }

    fun onClick(view: View) {
        GlobalScope.launch(Dispatchers.IO) {
            loadWeather()
        }
    }

    private suspend fun loadWeather() {
        val API_KEY = getString(R.string.api_key)
        val cityName = findViewById<EditText>(R.id.editTextCity).text.toString()

        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$API_KEY&units=metric"
        val stream = URL(weatherURL).openStream()

        val data = Scanner(stream).useDelimiter("\\A").next()
        val weatherData = Gson().fromJson(data, WeatherData::class.java)

        Log.d("mytag", weatherData.toString())
        withContext(Dispatchers.Main) {
            updateUI(weatherData)
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        Log.d("mytag", weatherData.toString())
        val sunriseTime = convertTime(weatherData.sys.sunrise)
        val sunsetTime = convertTime(weatherData.sys.sunset)
        val description = weatherData.weather[0].description
        val wind = weatherData.wind.speed.toString()

        val textViewSunrise = findViewById<TextView>(R.id.textViewSunrise)
        val textViewSunset = findViewById<TextView>(R.id.textViewSunset)
        val textViewDescription = findViewById<TextView>(R.id.textViewDescription)
        val textViewWind = findViewById<TextView>(R.id.textViewWind)

        textViewSunrise.text = "Sunrise Time: $sunriseTime"
        textViewSunset.text = "Sunset Time: $sunsetTime"
        textViewDescription.text = "Description: $description"
        textViewWind.text = "Wind speed: $wind"
    }
    private fun convertTime(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }
}
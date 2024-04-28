package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.roundToInt


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherView(vm: WeatherViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val focusManager = LocalFocusManager.current

    var locLat by remember { mutableStateOf("60.1699") }
    var locLon by remember { mutableStateOf("24.9384") }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val keyboardController = LocalSoftwareKeyboardController.current

    var cityQuery by remember { mutableStateOf("") }

    // Launch effect to get weather data
    // Only launch once
    // If location permission is granted, get location and use it to get weather data
    // If location permission is not granted, use default location
    LaunchedEffect(Unit) {
        if (!vm.effectLaunched) {
            if (locationPermissionState.status.isGranted) {
                val location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                if (location != null) {
                    locLat = location.latitude.toString()
                    locLon = location.longitude.toString()
                }
            }
            vm.getWeatherData(locLat, locLon)
            vm.effectLaunched = true
        }
    }

    // Main column
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // search bar and location button
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 0.dp, 8.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // search bar
            OutlinedTextField(
                modifier = Modifier
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0x00ffffff),
                    focusedBorderColor = Color(0x00ffffff),
                    unfocusedContainerColor = Color(0xffD8DEE9),
                    focusedContainerColor = Color(0xffECEFF4),
                    cursorColor = Color(0xff2E3440),
                    focusedTextColor = Color(0xff2E3440)
                ),
                value = cityQuery,
                onValueChange = {
                    cityQuery = it
                },
                placeholder = { Text(stringResource(R.string.search_bar_placeholder)) },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Get weather data for the city entered in the search bar
                        // Clear the search bar
                        // Hide the keyboard
                        // Clear focus
                        // If the search bar is empty, hide the keyboard and clear focus
                        // This is to prevent the keyboard from staying open after the user has finished using the search bar
                        if (cityQuery.isNotEmpty()) {
                            vm.getWeatherDataQuery(cityQuery)
                            cityQuery = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        if (cityQuery.isEmpty()) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    }
                )
            )
            // location button
            Button(
                modifier = Modifier
                    .padding(8.dp, 0.dp, 0.dp, 0.dp)
                    .width(56.dp)
                    .height(56.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffD8DEE9),
                    contentColor = Color(0xff2E3440)
                ),
                onClick = {
                    // Request location permission
                    locationPermissionState.launchPermissionRequest()
                    // If permission is granted, get location and use it to get weather data
                    if (locationPermissionState.status.isGranted) {
                        val location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                        if (location != null) {
                            locLat = location.latitude.toString()
                            locLon = location.longitude.toString()
                            vm.getWeatherData(locLat, locLon)
                        }
                    }
                }) {
                Icon(Icons.Rounded.LocationOn, contentDescription = stringResource(R.string.get_location_icon_desc), modifier = Modifier.size(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weather data
        if (vm.weatherItem != null && vm.forecastItem != null) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                CurrentWeatherModule(vm, navController)
                Spacer(modifier = Modifier.height(32.dp))
                ForecastModule(vm)
            }
        } else {
            Text(vm.errorMessage)
        }
    }
}

// Current weather module
// Displays current weather data
// Clicking on the module navigates to DetailsView
// Displays location, weather icon, temperature, weather description, high and low temperatures
@Composable
fun CurrentWeatherModule(vm: WeatherViewModel, navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(color = Color(0xffD8DEE9))
                .clickable(onClick = { navController.navigate("detailsView") })
                .padding(16.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Location
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = stringResource(R.string.location_icon_desc),
                    modifier = Modifier.size(26.dp),
                    tint = Color(0xff2E3440)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = vm.weatherItem!!.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xff2E3440)
                )
            }
            // Weather icon and temperature
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://openweathermap.org/img/wn/" + vm.weatherItem!!.weather[0].icon + "@4x.png"),
                    contentDescription = stringResource(R.string.weather_icon_desc),
                    modifier = Modifier.size(128.dp)
                )
                Text(
                    text = vm.weatherItem!!.main.temp.roundToInt().toString() + "°",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xff2E3440)
                )
            }
            // Weather description
            Text(
                text = vm.weatherItem!!.weather[0].main,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xff2E3440)
            )
            // High and low temperatures
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.high_temp_shortened) + vm.weatherItem!!.main.temp_max.roundToInt().toString() + "°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xff2E3440)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.low_temp_shortened) + vm.weatherItem!!.main.temp_min.roundToInt().toString() + "°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xff2E3440)
                )
            }
        }

        // Click me text
        Text(
            text = stringResource(R.string.click_me),
            fontSize = 12.sp,
            color = Color(0xff2E3440),
            modifier = Modifier
                .padding(8.dp, 4.dp, 8.dp, 4.dp)
                .align(Alignment.TopEnd)
                .alpha(0.15f)
        )
    }
}

// Forecast module
// Displays 24-hour forecast
// Displays time, weather description, low and high temperatures
@Composable
fun ForecastModule(vm: WeatherViewModel) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xffD8DEE9))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = stringResource(R.string.calendar_icon_desc),
                tint = Color(0xff2E3440),
                modifier = Modifier
                    .size(16.dp)
                    .alpha(0.5f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string._24_hour_forecast),
                color = Color(0xff2E3440),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.alpha(0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        // Forecast items
        // Displays time, weather description, low and high temperatures
        for (index in 0 until vm.forecastItem!!.cnt) {
            val item = vm.forecastItem!!.list[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp, 8.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time and weather description
                Row {
                    Text(
                        item.dt_txt.substringAfter(" ").substringBeforeLast(":"),
                        color = Color(0xff2E3440)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(
                        item.weather[0].main,
                        color = Color(0xff2E3440)
                    )
                }
                // Low and high temperatures
                Row(
                    modifier = Modifier.width(128.dp),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.low_temp_shortened) + item.main.temp_min.roundToInt()
                            .toString() + "°",
                        color = Color(0xff2E3440)
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(
                        stringResource(R.string.high_temp_shortened) + item.main.temp_max.roundToInt()
                            .toString() + "°",
                        color = Color(0xff2E3440)
                    )
                }
            }
            if (index < vm.forecastItem!!.cnt - 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Details view
// Displays additional weather data
// Displays low and high temperatures, feels like temperature, humidity, pressure, wind speed and wind direction
// Clicking on the back button navigates back to WeatherView
@Composable
fun DetailsView(vm: WeatherViewModel, navController: NavHostController) {
    if (vm.weatherItem != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color(0xffD8DEE9))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(stringResource(R.string.additional_details_for) + vm.weatherItem!!.name)
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Additional weather data
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color(0xffD8DEE9))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                // Low and high temperatures
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.temperature_low))
                    Text(text = vm.weatherItem!!.main.temp_min.roundToInt().toString() + "°C")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.temperature_high))
                    Text(text = vm.weatherItem!!.main.temp_max.roundToInt().toString() + "°C")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Feels like temperature
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.feels_like))
                    Text(text = vm.weatherItem!!.main.feels_like.roundToInt().toString() + "°C")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Humidity
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.humidity))
                    Text(text = vm.weatherItem!!.main.humidity.toString() + "%")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Pressure
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.pressure),
                        color = Color(0xff2E3440)
                    )
                    Text(
                        text = vm.weatherItem!!.main.pressure.toString() + " hPa",
                        color = Color(0xff2E3440)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Wind speed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.wind_speed),
                        color = Color(0xff2E3440)
                    )
                    Text(
                        text = vm.weatherItem!!.wind.speed.toString() + " m/s",
                        color = Color(0xff2E3440)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                // Wind direction
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.wind_direction),
                        color = Color(0xff2E3440)
                    )
                    Text(
                        text = vm.weatherItem!!.wind.deg.toString() + "°",
                        color = Color(0xff2E3440)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Back button
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = { navController.navigate("weatherView") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffD8DEE9),
                    contentColor = Color(0xff2E3440)
                )
            ) {
                Text(stringResource(R.string.back_button))
            }
        }
    }
}
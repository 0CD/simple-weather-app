package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val vm = WeatherViewModel()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Column (
                    Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xff9599A1),
                                    Color(0xffA3A8B0)
                                )
                            )
                        )
                        .fillMaxSize()
                ) {
                    WeatherAppView(vm)
                }
            }
        }
    }
}

// Navigation between WeatherView and DetailsView
@Composable
fun WeatherAppView(vm: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost( navController, startDestination = "weatherView" ) {
        composable("weatherView") { WeatherView(vm,navController) }
        composable("detailsView") { DetailsView(vm,navController) }
    }
}
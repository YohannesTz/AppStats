package com.github.yohannes.appstats.ui.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.yohannes.appstats.ui.activities.screens.detail.DetailScreen
import com.github.yohannes.appstats.ui.activities.screens.home.HomeScreen
import com.github.yohannes.appstats.ui.theme.AppStatsTheme
import com.github.yohannes.appstats.viewmodels.AppsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appsViewModel: AppsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        val state = appsViewModel.state.value
        if (!appsViewModel.grantStatus()) {
            Toast.makeText(this, "App not allowed", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        Log.e("state", state.toString())

        setContent {
            AppStatsTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "/") {
                    composable("/") {
                        HomeScreen(navController)
                    }

                    composable("/detail/{packageName}") {
                        DetailScreen()
                    }
                }
            }
        }
    }
}
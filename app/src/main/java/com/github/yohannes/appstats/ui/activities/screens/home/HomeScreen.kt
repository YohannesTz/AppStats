package com.github.yohannes.appstats.ui.activities.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.yohannes.appstats.ui.activities.screens.home.components.AppUsageListItem
import com.github.yohannes.appstats.viewmodels.AppsViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    appsViewModel: AppsViewModel = hiltViewModel(),
) {
    val state = appsViewModel.state.value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Log.e("isLoading", state.isLoading.toString())
        Log.e("state.ListSize", state.usagePercentagesList.size.toString())
        if (state.usagePercentagesList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Calculating...",
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.h6
                )
            }

        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.usagePercentagesList.size) { index ->
                    AppUsageListItem(state.usagePercentagesList[index], onClick = { packageName ->
                        navController.navigate("/detail/${packageName}")
                    })
                }
            }
        }
    }
}
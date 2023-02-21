package com.github.yohannes.appstats.ui.activities.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.yohannes.appstats.viewmodels.DetailScreenViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val values: List<FloatEntry> = state.weeklyUsageData.mapIndexed { index, value ->
        FloatEntry(
            index.toFloat(),
            value.usagePercentage.toFloat()
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberDrawablePainter(state.weeklyUsageData[0].icon),
                    contentDescription = "App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.weeklyUsageData[0].appName,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.h5
                )
                Spacer(modifier = Modifier.height(16.dp))

                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = MaterialTheme.colors.primary,
                                lineBackgroundShader = verticalGradient(
                                    arrayOf(
                                        MaterialTheme.colors.primary.copy(0.8f),
                                        MaterialTheme.colors.primary.copy(alpha = 0f)
                                    ),
                                )
                            )
                        )
                    ), model = entryModelOf(values)
                )

                Spacer(modifier = Modifier.height(16.dp))

                state.dayStatsList.forEach { stat ->
                    Text(text = stat.totalTime.toString())
                }
            }
        }
    }
}
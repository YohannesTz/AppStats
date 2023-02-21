package com.github.yohannes.appstats.ui.activities.screens.detail

import com.github.yohannes.appstats.data.models.DayStats
import com.github.yohannes.appstats.data.models.UsagePercentage

data class DetailScreenState(
    val weeklyUsageData: List<UsagePercentage> = emptyList(),
    val dayStatsList: List<DayStats> = emptyList(),
    val isLoading: Boolean = false
)
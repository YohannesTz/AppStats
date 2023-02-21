package com.github.yohannes.appstats.ui.activities.screens.home

import com.github.yohannes.appstats.data.models.UsagePercentage

data class UsagePercentageState(
    val usagePercentagesList: List<UsagePercentage> = emptyList(),
    val isLoading: Boolean = true
)
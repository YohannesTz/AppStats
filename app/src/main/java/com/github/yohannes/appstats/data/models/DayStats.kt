package com.github.yohannes.appstats.data.models

import java.util.*

data class DayStats(
    val packageName: String,
    val totalTime: Long,
    val lastUsed: Long,
    val date: Date
)

package com.github.yohannes.appstats.data.models

data class AppUsage(
    val app: App,
    val totalTime: Long,
    val lastUsed: Long
)
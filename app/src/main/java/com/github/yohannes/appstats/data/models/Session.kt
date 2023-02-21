package com.github.yohannes.appstats.data.models

data class Session(
    val startTimeInMillis: Long,
    val endTimeMillis: Long,
    val app: App
)
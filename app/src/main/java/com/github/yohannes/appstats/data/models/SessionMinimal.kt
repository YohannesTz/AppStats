package com.github.yohannes.appstats.data.models

data class SessionMinimal(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val packageName: String
)

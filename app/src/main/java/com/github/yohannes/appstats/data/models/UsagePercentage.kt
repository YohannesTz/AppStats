package com.github.yohannes.appstats.data.models

import android.graphics.drawable.Drawable

data class UsagePercentage(
    val icon: Drawable,
    val appName: String,
    val packageName: String,
    val usagePercentage: Int,
    val usageString: String
)

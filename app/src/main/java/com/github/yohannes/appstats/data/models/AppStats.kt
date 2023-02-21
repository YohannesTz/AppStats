package com.github.yohannes.appstats.data.models

import android.app.usage.ExternalStorageStats
import android.app.usage.NetworkStats
import android.app.usage.StorageStats
import android.app.usage.UsageStats
import android.graphics.drawable.Drawable

data class AppStats(
    val packageName: String,
    val usageStats: UsageStats,
    val networkStats: NetworkStats,
    val storageStats: StorageStats,
    val externalStorageStats: ExternalStorageStats,
    val appIcon: Drawable
)
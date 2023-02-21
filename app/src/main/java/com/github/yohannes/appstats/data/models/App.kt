package com.github.yohannes.appstats.data.models

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log

data class App(
    val packageName: String,
    val appName: String,
    var iconDrawable: Drawable?
) {
    fun setDrawableIfNull(context: Context): App {
        if (iconDrawable == null) {
            try {
                val packageManger = context.packageManager
                iconDrawable = packageManger.getApplicationIcon(packageName)
            } catch (e: Exception) {
                Log.e("getDrawable", "Failed to load drawable for $packageName")
            }
        }
        return this
    }

    companion object {
        fun fromContext(
            context: Context,
            packageName: String
        ): App {
            var applicationIcon: Drawable? = null
            var applicationName: String

            try {
                val packageManager = context.packageManager
                applicationIcon = packageManager.getApplicationIcon(packageName)
                val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)) as ApplicationInfo
                } else {
                    packageManager.getPackageInfo(packageName, 0) as ApplicationInfo
                }
                applicationName = packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: Exception) {
                applicationName = "$packageName is probably uninstalled"
            }

            return App(
                packageName,
                applicationName,
                applicationIcon
            )
        }
    }
}
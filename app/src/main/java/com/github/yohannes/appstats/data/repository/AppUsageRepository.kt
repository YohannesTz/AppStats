package com.github.yohannes.appstats.data.repository

import android.Manifest
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.util.Log
import com.github.yohannes.appstats.data.models.AppUsage
import com.github.yohannes.appstats.data.models.DayStats
import com.github.yohannes.appstats.data.models.UsagePercentage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue


class AppUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    suspend fun getUsageList(): List<AppUsage> {
        return withContext(Dispatchers.Default) {
            val usageList = arrayListOf<AppUsage>()
            val currentTimeInMillis = System.currentTimeMillis()

            val cal = Calendar.getInstance()
            cal.timeInMillis = currentTimeInMillis
            cal.add(Calendar.MONTH, -1)
            return@withContext usageList
        }
    }

    fun getAppUsageDayByDayMonthly(appPackageName: String): Flow<List<DayStats>> = flow {
        var statsList = arrayListOf<DayStats>()
        val startTimeMillis = System.currentTimeMillis() - 1000 * 3600 * 24 * 8

        val eventsMap = mutableMapOf<String, MutableList<UsageEvents.Event>>()
        val events = usageStatsManager.queryEvents(startTimeMillis, System.currentTimeMillis())

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            val packageName = event.packageName

            (eventsMap[packageName] ?: mutableListOf()).let {
                it.add(event)
                eventsMap[packageName] = it
            }
        }

        eventsMap.forEach { (packageName, events) ->
            val pm = context.packageManager

            if (pm.getLaunchIntentForPackage(packageName) != null) {
                var startTime = 0L
                var endTime = 0L
                var totalTime = 0L
                var lastUsed = 0L
                var isInitialized = false
                events.forEach { event ->
                    when (event.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> { // same as MOVE_TO_FOREGROUND
                            // start time
                            isInitialized = true
                            startTime = event.timeStamp
                            endTime = 0L
                        }

                        UsageEvents.Event.ACTIVITY_PAUSED -> { // same as MOVE_TO_BACKGROUND
                            // end time
                            if (startTime == 0L) {
                                if (!isInitialized) {
                                    startTime = startTimeMillis
                                    endTime = event.timeStamp
                                    lastUsed = endTime
                                }
                            } else {
                                endTime = event.timeStamp
                                lastUsed = endTime
                            }
                        }
                    }

                    // If both start and end times exist, add the time to totalTime
                    // and reset start and end times
                    if (startTime != 0L && endTime != 0L) {
                        totalTime += endTime - startTime
                        startTime = 0L; endTime = 0L
                    }
                }

                // If the end time was not found, it's likely that the app is still running
                // so assume the end time to be now
                if (startTime != 0L && endTime == 0L) {
                    lastUsed = System.currentTimeMillis()
                    totalTime += lastUsed - startTime
                }

                // If total time is more than 1 second
                if (totalTime >= 1000) {
                    val stats = DayStats(
                        packageName,
                        totalTime,
                        lastUsed,
                        Date()
                    )
                    statsList.add(stats)
                }
            }
        }

        statsList = statsList.stream().filter { it.packageName == appPackageName }
            .collect(Collectors.toList()) as ArrayList<DayStats>

        Log.e("statsList", statsList.size.toString())
        emit(statsList)
    }

    fun getAppUsageWeeklyNoFlow(packageName: String): List<UsagePercentage> {
        var appUsageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 3600 * 24 * 8,
            System.currentTimeMillis()
        )

        val totalTime: Long = appUsageStatsList.stream().map(UsageStats::getTotalTimeInForeground)
            .mapToLong(Long::absoluteValue).sum()

        appUsageStatsList =
            appUsageStatsList.stream().filter { app ->
                app.packageName.equals(packageName)
            }
                .collect(Collectors.toList())


        val appUsageWeekly = arrayListOf<UsagePercentage>()
        val applicationInfo =
            context.packageManager.getApplicationInfo(packageName, 0)

        appUsageStatsList.forEach { usageStat ->
            appUsageWeekly.add(
                UsagePercentage(
                    context.packageManager.getApplicationIcon(packageName),
                    context.packageManager.getApplicationLabel(applicationInfo).toString(),
                    packageName,
                    (usageStat.totalTimeInForeground * 100 / totalTime).toInt(),
                    getDurationBreakdown(usageStat.totalTimeInForeground)
                )
            )
        }

        return appUsageWeekly
    }

    fun getAppUsageWeekly(packageName: String): Flow<List<UsagePercentage>> = flow {
        var appUsageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 3600 * 24 * 8,
            System.currentTimeMillis()
        )

        val totalTime: Long = appUsageStatsList.stream().map(UsageStats::getTotalTimeInForeground)
            .mapToLong(Long::absoluteValue).sum()

        appUsageStatsList =
            appUsageStatsList.stream().filter { app ->
                app.totalTimeInForeground >= 0 && app.packageName.equals(packageName)
            }
                .collect(Collectors.toList())


        val appUsageWeekly = arrayListOf<UsagePercentage>()
        val applicationInfo =
            context.packageManager.getApplicationInfo(packageName, 0)

        appUsageStatsList.forEach { usageStat ->
            appUsageWeekly.add(
                UsagePercentage(
                    context.packageManager.getApplicationIcon(packageName),
                    context.packageManager.getApplicationLabel(applicationInfo).toString(),
                    packageName,
                    (usageStat.totalTimeInForeground * 100 / totalTime).toInt(),
                    getDurationBreakdown(usageStat.totalTimeInForeground)
                )
            )
        }

        emit(appUsageWeekly)
    }

    fun getUsageStatsMap(): Map<String, UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        var appUsageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            //System.currentTimeMillis() - 1000 * 3600 * 24,
            cal.timeInMillis,
            System.currentTimeMillis()
        )
        appUsageStatsList =
            appUsageStatsList.stream()
                .collect(Collectors.toList())

        val usageMaps: HashMap<String, UsageStats> = hashMapOf()

        if (appUsageStatsList.size > 0) {
            appUsageStatsList.forEach { usageStats ->
                usageMaps[usageStats.packageName] = usageStats
            }
        }

        return usageMaps
    }

    fun getUsageWithPercent(usageMap: Map<String, UsageStats>): Flow<List<UsagePercentage>> = flow {
        val usageWithPercentage = arrayListOf<UsagePercentage>()
        val usageList = usageMap.values

        val totalTime: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            usageList.stream().map { it.totalTimeInForeground + it.totalTimeVisible }
                .mapToLong(Long::absoluteValue).sum()
        } else {
            usageList.stream().map(UsageStats::getTotalTimeInForeground)
                .mapToLong(Long::absoluteValue).sum()
        }
        Log.e("totalTime", totalTime.toString())

        usageList.forEach { usageStat ->
            try {
                if (isAppInfoAvailable(usageStat)) {
                    val applicationInfo =
                        context.packageManager.getApplicationInfo(usageStat.packageName, 0)

                    val usagePercentage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        (usageStat.totalTimeInForeground + usageStat.totalTimeVisible) / totalTime * 100
                    } else {
                        (usageStat.totalTimeInForeground) / totalTime * 100
                    }
                    val usageString = getDurationBreakdown(usageStat.totalTimeInForeground)
                    usageWithPercentage.add(
                        UsagePercentage(
                            context.packageManager.getApplicationIcon(usageStat.packageName),
                            context.packageManager.getApplicationLabel(applicationInfo).toString(),
                            usageStat.packageName,
                            usagePercentage.toInt(),
                            usageString
                        )
                    )
                }
            } catch (ex: Exception) {
                Log.e("exception", ex.message.toString())
            }
        }

        //usageWithPercentage.reverse()
        val usageWithPercentageSorted =
            usageWithPercentage.sortedWith(compareBy { it.usagePercentage }).reversed()
        Log.e("finished", "List Size ${usageWithPercentage.size}")

        emit(usageWithPercentageSorted)
    }

     fun getUsageWithPercentageNoFlow(usageMap: Map<String, UsageStats>): List<UsagePercentage> {
        val usageWithPercentage = arrayListOf<UsagePercentage>()
        val usageList = usageMap.values

        val totalTime: Long = usageList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::toLong).sum()
        Log.e("totalTime", totalTime.toString())

        usageList.forEach { usageStat ->
            try {
                if (isAppInfoAvailable(usageStat)) {
                    val applicationInfo =
                        context.packageManager.getApplicationInfo(usageStat.packageName, 0)

                    val usagePercentage = (usageStat.totalTimeInForeground * 100 / totalTime)
                    val usageString = getDurationBreakdown(usageStat.totalTimeInForeground)
                    usageWithPercentage.add(
                        UsagePercentage(
                            context.packageManager.getApplicationIcon(usageStat.packageName),
                            context.packageManager.getApplicationLabel(applicationInfo)
                                .toString(),
                            usageStat.packageName,
                            usagePercentage.toInt(),
                            usageString
                        )
                    )
                }
            } catch (ex: Exception) {
                Log.e("exception", ex.message.toString())
            }
        }

        usageWithPercentage.reverse()
        Log.e("finished", "List Size ${usageWithPercentage.size}")

        return usageWithPercentage
    }

    suspend fun getUsageWithPercentage(usageMap: Map<String, UsageStats>): Flow<List<UsagePercentage>> =
        flow {
            //return withContext(Dispatchers.Default) {
            val usageWithPercentage = arrayListOf<UsagePercentage>()
            val usageList = usageMap.values

            val totalTime: Long = usageList.stream().map(UsageStats::getTotalTimeInForeground)
                .mapToLong(Long::absoluteValue).sum()
            Log.e("totalTime", totalTime.toString())

            usageList.forEach { usageStat ->
                try {
                    if (isAppInfoAvailable(usageStat)) {
                        val applicationInfo =
                            context.packageManager.getApplicationInfo(usageStat.packageName, 0)

                        val usagePercentage = (usageStat.totalTimeInForeground * 100 / totalTime)
                        val usageString = getDurationBreakdown(usageStat.totalTimeInForeground)
                        usageWithPercentage.add(
                            UsagePercentage(
                                context.packageManager.getApplicationIcon(usageStat.packageName),
                                context.packageManager.getApplicationLabel(applicationInfo)
                                    .toString(),
                                usageStat.packageName,
                                usagePercentage.toInt(),
                                usageString
                            )
                        )
                    }
                } catch (ex: Exception) {
                    Log.e("exception", ex.message.toString())
                }
            }

            usageWithPercentage.reverse()
            Log.e("finished", "List Size ${usageWithPercentage.size}")

            //@withContext usageWithPercentage
            emit(usageWithPercentage)
            //}
        }

    fun grantStatus(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        //val mode = appOps.unsafeCheckOp(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        val mode = appOps.checkOpNoThrow(
            OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )

        return if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(
                Manifest.permission.PACKAGE_USAGE_STATS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == MODE_ALLOWED
        }
    }

    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try {
            context.packageManager
                .getApplicationInfo(usageStats.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getDurationBreakdown(timeInput: Long): String {
        val millis = timeInput
        if (millis < 0) {
            throw IllegalArgumentException("Duration must be greater than zero")
        }

        val s = millis / 1000
        val hours = s / 3600
        val minutes = (s % 3600) / 60
        val seconds = (s % 60)

        return "$hours h $minutes m $seconds s"
    }
}
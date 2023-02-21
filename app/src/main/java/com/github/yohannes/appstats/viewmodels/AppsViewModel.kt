package com.github.yohannes.appstats.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yohannes.appstats.data.models.UsagePercentage
import com.github.yohannes.appstats.data.repository.AppUsageRepository
import com.github.yohannes.appstats.ui.activities.screens.home.UsagePercentageState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val appsUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _state = mutableStateOf(UsagePercentageState())
    val state: State<UsagePercentageState> = _state

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _state.value = state.value.copy(
                    isLoading = true
                )
            }

            withContext(Dispatchers.Default) {
                val list = getUsagePercentageWithoutFlow()
                _state.value = state.value.copy(
                    usagePercentagesList = list
                )
            }

            withContext(Dispatchers.Main) {
                _state.value = state.value.copy(
                    isLoading = false
                )
            }
        }
    }

    private suspend fun getUsage() {
        getUsageOfApps().onStart {
            _state.value = state.value.copy(
                isLoading = true
            )
        }.onEach { usagePercentageList ->
            _state.value = state.value.copy(
                usagePercentagesList = state.value.usagePercentagesList + usagePercentageList,
            )
        }.onCompletion {
            _state.value = state.value.copy(
                isLoading = false
            )
        }.launchIn(viewModelScope)

    }

    fun grantStatus(): Boolean = appsUsageRepository.grantStatus()

    private suspend fun getUsageOfApps(): Flow<List<UsagePercentage>> {
        val appUsageMap = appsUsageRepository.getUsageStatsMap()
        return appsUsageRepository.getUsageWithPercent(appUsageMap)
    }

    private suspend fun getUsagePercentageWithoutFlow(): List<UsagePercentage> {
        val appUsageMap = appsUsageRepository.getUsageStatsMap()
        Log.e("appUsageMapSize", appUsageMap.size.toString())
        return appsUsageRepository.getUsageWithPercentageNoFlow(appUsageMap)
    }
}
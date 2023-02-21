package com.github.yohannes.appstats.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yohannes.appstats.data.models.DayStats
import com.github.yohannes.appstats.data.models.UsagePercentage
import com.github.yohannes.appstats.data.repository.AppUsageRepository
import com.github.yohannes.appstats.ui.activities.screens.detail.DetailScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val appsUsageRepository: AppUsageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(DetailScreenState())
    val state: State<DetailScreenState> = _state

    init {
        savedStateHandle.get<String>("packageName")?.let {
            //getData(it)
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    _state.value = state.value.copy(
                        isLoading = true
                    )
                }

                withContext(Dispatchers.Default) {
                    val list = getUsageDetailOfAnAppNoFlow(it)
                    _state.value = state.value.copy(
                        weeklyUsageData = list,
                    )
                }

                withContext(Dispatchers.Main) {
                    _state.value = state.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getData(p: String) {

        getUsageDetailOfAnApp(p).onStart {
            _state.value = state.value.copy(
                isLoading = true
            )
        }.onEach { usagePercentageList ->
            withContext(Dispatchers.Default) {
                _state.value = state.value.copy(
                    weeklyUsageData = state.value.weeklyUsageData + usagePercentageList,
                )
            }
        }.onCompletion {
            _state.value = state.value.copy(
                isLoading = false
            )
        }.launchIn(viewModelScope)

        /*getAppUsageDayByDay(p).onStart {
            _state.value = state.value.copy(
                isLoading = true
            )
        }.onEach { dayStats ->
            _state.value = state.value.copy(
                dayStatsList = state.value.dayStatsList + dayStats,
            )
        }.onCompletion {
            _state.value = state.value.copy(
                isLoading = false
            )
        }*/
    }

    private fun getAppUsageDayByDay(packageName: String): Flow<List<DayStats>> {
        return appsUsageRepository.getAppUsageDayByDayMonthly(packageName)
    }

    private suspend fun getUsageDetailOfAnAppNoFlow(packageName: String): List<UsagePercentage> {
        return appsUsageRepository.getAppUsageWeeklyNoFlow(packageName)
    }

    private fun getUsageDetailOfAnApp(packageName: String): Flow<List<UsagePercentage>> {
        return appsUsageRepository.getAppUsageWeekly(packageName)
    }
}
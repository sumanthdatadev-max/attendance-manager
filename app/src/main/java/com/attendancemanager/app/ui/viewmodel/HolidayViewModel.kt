package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancemanager.app.data.entity.Holiday
import com.attendancemanager.app.repository.HolidayRepository
import com.attendancemanager.app.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HolidayViewModel(
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    val holidays: StateFlow<List<Holiday>> = holidayRepository.getAllHolidays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHoliday(date: String, description: String) {
        viewModelScope.launch {
            holidayRepository.setHoliday(date, description.ifBlank { "Holiday" })
        }
    }

    fun removeHoliday(date: String) {
        viewModelScope.launch {
            holidayRepository.removeHoliday(date)
        }
    }
}

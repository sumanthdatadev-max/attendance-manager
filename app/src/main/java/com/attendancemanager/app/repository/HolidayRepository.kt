package com.attendancemanager.app.repository

import com.attendancemanager.app.data.dao.HolidayDao
import com.attendancemanager.app.data.entity.Holiday
import kotlinx.coroutines.flow.Flow

class HolidayRepository(private val holidayDao: HolidayDao) {

    fun getAllHolidays(): Flow<List<Holiday>> = holidayDao.getAllHolidays()

    suspend fun getAllHolidaysOnce(): List<Holiday> = holidayDao.getAllHolidaysOnce()

    fun observeHolidayForDate(date: String): Flow<Holiday?> = holidayDao.observeHolidayForDate(date)

    suspend fun isHoliday(date: String): Boolean = holidayDao.isHoliday(date)

    fun getHolidaysInRange(startDate: String, endDate: String): Flow<List<Holiday>> =
        holidayDao.getHolidaysInRange(startDate, endDate)

    suspend fun getHolidaysInRangeOnce(startDate: String, endDate: String): List<Holiday> =
        holidayDao.getHolidaysInRangeOnce(startDate, endDate)

    suspend fun setHoliday(date: String, description: String) =
        holidayDao.insert(Holiday(date, description))

    suspend fun removeHoliday(date: String) = holidayDao.removeHoliday(date)

    suspend fun restoreAll(holidays: List<Holiday>) {
        holidayDao.deleteAll()
        holidays.forEach { holidayDao.insert(it) }
    }
}

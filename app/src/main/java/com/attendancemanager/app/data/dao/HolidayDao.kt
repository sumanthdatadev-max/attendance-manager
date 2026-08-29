package com.attendancemanager.app.data.dao

import androidx.room.*
import com.attendancemanager.app.data.entity.Holiday
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holiday: Holiday)

    @Query("DELETE FROM holidays WHERE date = :date")
    suspend fun removeHoliday(date: String)

    @Query("SELECT * FROM holidays ORDER BY date DESC")
    fun getAllHolidays(): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays ORDER BY date DESC")
    suspend fun getAllHolidaysOnce(): List<Holiday>

    @Query("SELECT * FROM holidays WHERE date = :date LIMIT 1")
    fun observeHolidayForDate(date: String): Flow<Holiday?>

    @Query("SELECT EXISTS(SELECT 1 FROM holidays WHERE date = :date)")
    suspend fun isHoliday(date: String): Boolean

    @Query("SELECT * FROM holidays WHERE date BETWEEN :startDate AND :endDate")
    fun getHolidaysInRange(startDate: String, endDate: String): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getHolidaysInRangeOnce(startDate: String, endDate: String): List<Holiday>

    @Query("DELETE FROM holidays")
    suspend fun deleteAll()
}

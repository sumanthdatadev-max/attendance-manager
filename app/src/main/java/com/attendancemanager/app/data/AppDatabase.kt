package com.attendancemanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.attendancemanager.app.data.dao.AttendanceDao
import com.attendancemanager.app.data.dao.FeeDao
import com.attendancemanager.app.data.dao.HolidayDao
import com.attendancemanager.app.data.dao.MemberDao
import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.Holiday
import com.attendancemanager.app.data.entity.Member

@Database(
    entities = [Member::class, AttendanceRecord::class, Holiday::class, Fee::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun holidayDao(): HolidayDao
    abstract fun feeDao(): FeeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attendance_manager.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.attendancemanager.app.backup

import android.content.Context
import android.net.Uri
import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.data.entity.Holiday
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.HolidayRepository
import com.attendancemanager.app.repository.MemberRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class BackupResult(val success: Boolean, val message: String)

class BackupManager(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val holidayRepository: HolidayRepository
) {

    companion object {
        const val BACKUP_VERSION = 1
        const val SUGGESTED_FILE_NAME = "attendance_manager_backup.json"
    }

    suspend fun exportTo(context: Context, uri: Uri): BackupResult {
        return try {
            val json = buildBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toString(2).toByteArray(Charsets.UTF_8))
            } ?: return BackupResult(false, "Could not open file for writing")
            BackupResult(true, "Backup exported successfully")
        } catch (e: Exception) {
            BackupResult(false, "Export failed: ${e.message}")
        }
    }

    private suspend fun buildBackupJson(): JSONObject {
        val members = memberRepository.getAllMembersOnce()
        val records = attendanceRepository.getAllRecordsOnce()
        val holidays = holidayRepository.getAllHolidaysOnce()

        val root = JSONObject()
        root.put("backupVersion", BACKUP_VERSION)

        val membersArray = JSONArray()
        members.forEach { m ->
            val obj = JSONObject()
            obj.put("memberId", m.memberId)
            obj.put("name", m.name)
            obj.put("mobile", m.mobile ?: JSONObject.NULL)
            obj.put("joiningDate", m.joiningDate)
            obj.put("leavingDate", m.leavingDate ?: JSONObject.NULL)
            obj.put("isActive", m.isActive)
            membersArray.put(obj)
        }
        root.put("members", membersArray)

        val recordsArray = JSONArray()
        records.forEach { r ->
            val obj = JSONObject()
            obj.put("memberId", r.memberId)
            obj.put("date", r.date)
            obj.put("status", r.status.name)
            recordsArray.put(obj)
        }
        root.put("attendanceRecords", recordsArray)

        val holidaysArray = JSONArray()
        holidays.forEach { h ->
            val obj = JSONObject()
            obj.put("date", h.date)
            obj.put("description", h.description)
            holidaysArray.put(obj)
        }
        root.put("holidays", holidaysArray)

        return root
    }

    suspend fun importFrom(context: Context, uri: Uri): BackupResult {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            } ?: return BackupResult(false, "Could not open file for reading")

            val root = JSONObject(text)

            val membersArray = root.optJSONArray("members") ?: JSONArray()
            val members = mutableListOf<Member>()
            for (i in 0 until membersArray.length()) {
                val obj = membersArray.getJSONObject(i)
                members.add(
                    Member(
                        memberId = obj.getString("memberId"),
                        name = obj.getString("name"),
                        mobile = if (obj.isNull("mobile")) null else obj.getString("mobile"),
                        joiningDate = obj.getString("joiningDate"),
                        leavingDate = if (obj.isNull("leavingDate")) null else obj.getString("leavingDate"),
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }

            val recordsArray = root.optJSONArray("attendanceRecords") ?: JSONArray()
            val records = mutableListOf<AttendanceRecord>()
            for (i in 0 until recordsArray.length()) {
                val obj = recordsArray.getJSONObject(i)
                records.add(
                    AttendanceRecord(
                        memberId = obj.getString("memberId"),
                        date = obj.getString("date"),
                        status = AttendanceStatus.valueOf(obj.getString("status"))
                    )
                )
            }

            val holidaysArray = root.optJSONArray("holidays") ?: JSONArray()
            val holidays = mutableListOf<Holiday>()
            for (i in 0 until holidaysArray.length()) {
                val obj = holidaysArray.getJSONObject(i)
                holidays.add(
                    Holiday(
                        date = obj.getString("date"),
                        description = obj.optString("description", "Holiday")
                    )
                )
            }

            // Restore replaces existing data with the backup contents.
            memberRepository.restoreAll(members)
            attendanceRepository.restoreAll(records)
            holidayRepository.restoreAll(holidays)

            BackupResult(true, "Restored ${members.size} members, ${records.size} attendance records, ${holidays.size} holidays")
        } catch (e: Exception) {
            BackupResult(false, "Restore failed: ${e.message}")
        }
    }
}

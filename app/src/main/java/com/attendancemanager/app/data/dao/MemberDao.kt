package com.attendancemanager.app.data.dao

import androidx.room.*
import com.attendancemanager.app.data.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(member: Member)

    @Update
    suspend fun update(member: Member)

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE memberId = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: String): Member?

    @Query("SELECT * FROM members WHERE memberId = :memberId LIMIT 1")
    fun observeMemberById(memberId: String): Flow<Member?>

    @Query("SELECT COUNT(*) FROM members WHERE memberId = :memberId")
    suspend fun countById(memberId: String): Int

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    fun observeActiveCount(): Flow<Int>

    /**
     * Members eligible on [date]: joined on/before date, and (no leaving date OR
     * leaving date is on/after date). Works for both active and formerly-active
     * members so historical attendance dates still show the right roster.
     */
    @Query(
        """
        SELECT * FROM members
        WHERE joiningDate <= :date
        AND (leavingDate IS NULL OR leavingDate >= :date)
        ORDER BY name ASC
        """
    )
    fun getMembersEligibleOnDate(date: String): Flow<List<Member>>

    @Query(
        """
        SELECT * FROM members
        WHERE joiningDate <= :date
        AND (leavingDate IS NULL OR leavingDate >= :date)
        ORDER BY name ASC
        """
    )
    suspend fun getMembersEligibleOnDateOnce(date: String): List<Member>

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR memberId LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchMembers(query: String): Flow<List<Member>>

    @Query("DELETE FROM members")
    suspend fun deleteAll()

    @Query("SELECT * FROM members")
    suspend fun getAllMembersOnce(): List<Member>
}

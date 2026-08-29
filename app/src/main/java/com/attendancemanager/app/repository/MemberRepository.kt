package com.attendancemanager.app.repository

import com.attendancemanager.app.data.dao.MemberDao
import com.attendancemanager.app.data.entity.Member
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {

    fun getAllMembers(): Flow<List<Member>> = memberDao.getAllMembers()

    fun getActiveMembers(): Flow<List<Member>> = memberDao.getActiveMembers()

    fun getMembersEligibleOnDate(date: String): Flow<List<Member>> =
        memberDao.getMembersEligibleOnDate(date)

    suspend fun getMembersEligibleOnDateOnce(date: String): List<Member> =
        memberDao.getMembersEligibleOnDateOnce(date)

    fun searchMembers(query: String): Flow<List<Member>> = memberDao.searchMembers(query)

    fun observeMember(memberId: String): Flow<Member?> = memberDao.observeMemberById(memberId)

    suspend fun getMember(memberId: String): Member? = memberDao.getMemberById(memberId)

    fun observeActiveCount(): Flow<Int> = memberDao.observeActiveCount()

    suspend fun isMemberIdTaken(memberId: String): Boolean = memberDao.countById(memberId) > 0

    suspend fun addMember(member: Member) = memberDao.insert(member)

    suspend fun updateMember(member: Member) = memberDao.update(member)

    suspend fun getAllMembersOnce(): List<Member> = memberDao.getAllMembersOnce()

    suspend fun restoreAll(members: List<Member>) {
        memberDao.deleteAll()
        members.forEach { memberDao.insert(it) }
    }
}

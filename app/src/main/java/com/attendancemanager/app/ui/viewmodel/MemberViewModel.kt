package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.MemberRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MemberViewModel(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val members: StateFlow<List<Member>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) memberRepository.getAllMembers()
            else memberRepository.searchMembers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult: StateFlow<String?> = _saveResult

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun memberHistory(memberId: String): StateFlow<List<AttendanceRecord>> =
        attendanceRepository.getAttendanceForMember(memberId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getMember(memberId: String): Member? = memberRepository.getMember(memberId)

    fun addMember(
        memberId: String,
        name: String,
        mobile: String?,
        joiningDate: String,
        classNumber: Int = 1,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedId = memberId.trim()
            val trimmedName = name.trim()
            if (trimmedId.isBlank()) {
                onResult(false, "Member ID is required"); return@launch
            }
            if (trimmedName.isBlank()) {
                onResult(false, "Name is required"); return@launch
            }
            if (memberRepository.isMemberIdTaken(trimmedId)) {
                onResult(false, "Member ID already exists"); return@launch
            }
            memberRepository.addMember(
                Member(
                    memberId = trimmedId,
                    name = trimmedName,
                    mobile = mobile?.trim()?.ifBlank { null },
                    classNumber = classNumber,
                    joiningDate = joiningDate,
                    leavingDate = null,
                    isActive = true
                )
            )
            onResult(true, "Member added")
        }
    }

    fun updateMember(
        member: Member,
        name: String,
        mobile: String?,
        joiningDate: String,
        leavingDate: String?,
        isActive: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) {
                onResult(false, "Name is required"); return@launch
            }
            memberRepository.updateMember(
                member.copy(
                    name = trimmedName,
                    mobile = mobile?.trim()?.ifBlank { null },
                    joiningDate = joiningDate,
                    leavingDate = leavingDate,
                    isActive = isActive
                )
            )
            onResult(true, "Member updated")
        }
    }
}

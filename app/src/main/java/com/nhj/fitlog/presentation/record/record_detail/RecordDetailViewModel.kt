package com.nhj.fitlog.presentation.record.record_detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.RecordService
import com.nhj.fitlog.domain.model.ExerciseDayRecordModel
import com.nhj.fitlog.utils.RecordScreenName
import com.nhj.fitlog.utils.RoutineExerciseWithSets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class UserPublicFlags(
    val nickName: String,
    val picturePublic: Boolean,
    val recordPublic: Boolean
)

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val recordService: RecordService
) : ViewModel() {

    val application = context as FitLogApplication

    val uid = mutableStateOf<String>("")
    val previousScreen = mutableStateOf<String>("")

    val picturePublic = mutableStateOf<Boolean>(false)
    val recordPublic = mutableStateOf<Boolean>(false)

    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val record = mutableStateOf<ExerciseDayRecordModel?>(null)
    val items = mutableStateOf<List<RoutineExerciseWithSets>>(emptyList())
    
    // 탑바 메뉴 상태
    val showTopBarMenu = mutableStateOf(false)
    // 삭제 확인 다이얼로그
    val showDeleteConfirm = mutableStateOf(false)

    fun load(recordId: String) {
        if (loading.value) return
        viewModelScope.launch {
            try {
                loading.value = true
                error.value = null

                // 1) 먼저 공개 플래그 로드
                val flags = recordService.getUserPublicFlags(uid.value)
                picturePublic.value = flags.picturePublic
                recordPublic.value  = flags.recordPublic

                // 2) 그 다음 기록 상세 로드
                val detail = recordService.getDayRecordDetail(uid.value, recordId)
                record.value = detail.record
                items.value  = detail.items

            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }

    // 기록 삭제
    fun onDeleteClick() {
        val id = record.value?.recordId ?: return
        if (loading.value) return

        viewModelScope.launch {
            try {
                loading.value = true
                error.value = null
                // 소프트 삭제
                recordService.deleteDayRecord(uid.value, id)
                // 뒤로가기
                onBack()
            } catch (t: Throwable) {
                error.value = t.message
            } finally {
                loading.value = false
            }
        }
    }


    // 날짜 시간 표시용: "yyyy / MM / dd"
    // 표시용
    fun formatDateDisplay(iso: String): String = try {
        java.time.LocalDate.parse(iso, DateTimeFormatter.ISO_DATE)
            .format(DateTimeFormatter.ofPattern("yyyy / MM / dd"))
    } catch (_: Throwable) {
        iso
    }

    fun formatTime12hKST(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("a hh:mm").withLocale(java.util.Locale.KOREAN))

    fun onBack() = application.navHostController.popBackStack()

    fun onNavigateToEditScreen() = application.navHostController.navigate(
        RecordScreenName.RECORD_EDIT_SCREEN.name +
                "/${record.value?.recordId}"
    )
}
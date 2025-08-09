package com.nhj.fitlog.presentation.routine.list

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.domain.model.RoutineModel
import com.nhj.fitlog.domain.vo.RoutineVO
import com.nhj.fitlog.utils.RoutineScreenName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val application = context as FitLogApplication

    // 루틴 리스트
    val routines = mutableStateOf<List<RoutineModel>>(emptyList())

    // users/{uid}/routines 실시간 구독
    fun startRoutineListener() {
        val db = FirebaseFirestore.getInstance()
        var listener: ListenerRegistration? = null

        listener?.remove()

        val uid = application.userUid

        listener = db.collection("users")
            .document(uid)                    // ✅ uid 경로 고정
            .collection("routines")
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("RoutineListVM", "listener error", err)
                    return@addSnapshotListener
                }
                val updated = snap?.documents
                    ?.mapNotNull { it.toObject(RoutineVO::class.java)?.toModel() }
                    .orEmpty()
                routines.value = updated
                Log.d("RoutineListVM2", "${routines.value}")
            }
    }

    // 루틴 삭제
    fun deleteRoutine(routineId: String) {
        val db = FirebaseFirestore.getInstance()
        val uid = application.userUid
        db.collection("users").document(uid)
            .collection("routines").document(routineId)
            .delete()
            .addOnFailureListener { Log.e("RoutineListVM", "delete failed", it) }
    }

    // 네비게이션
    fun onNavigateRoutineAdd() {
        application.navHostController.navigate(RoutineScreenName.ROUTINE_ADD_SCREEN.name)
    }
    fun onNavigateRoutineDetail() {
        // application.navHostController.navigate("ROUTINE_DETAIL_SCREEN/$routineId")
    }
    fun onNavigateRoutineReorder(routineId: String) {
        application.navHostController.navigate("ROUTINE_REORDER_SCREEN/$routineId")
    }
    fun onBackNavigation() {
        application.navHostController.popBackStack()
    }
}

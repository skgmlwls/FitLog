package com.nhj.fitlog.presentation.routine.exercise_list

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.data.service.UserService
import com.nhj.fitlog.domain.model.ExerciseTypeModel
import com.nhj.fitlog.domain.vo.ExerciseTypeVO
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class RoutineAddListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val exerciseService: ExerciseService
) : ViewModel() {
    val application = context as FitLogApplication
    val list = mutableStateOf<List<ExerciseTypeModel>>(emptyList())

    // Firestore 리스너 등록 객체
    private var exerciseListener: ListenerRegistration? = null

    fun load() {
        val uid = application.userUid ?: return

        exerciseListener = FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .collection("exerciseType")
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("ExerciseTypeViewModel", "listener error", err)
                    return@addSnapshotListener
                }
                snap?.documents
                    ?.mapNotNull { it.toObject(ExerciseTypeVO::class.java)?.toModel() }
                    ?.sortedBy { it.name }
                    ?.also { updated ->
                        list.value = updated
                        Log.d("ExerciseTypeViewModel", "exercises updated: ${updated.size}")
                    }
            }
    }

    fun selectAndBack(id: String, name: String, category: String, memo: String) {
        val nav = application.navHostController
        nav.previousBackStackEntry?.savedStateHandle?.set("selectedExerciseId", id)
        nav.previousBackStackEntry?.savedStateHandle?.set("selectedExerciseName", name)
        nav.previousBackStackEntry?.savedStateHandle?.set("selectedExerciseCategory", category)
        nav.previousBackStackEntry?.savedStateHandle?.set("selectedExerciseMemo", memo)
        nav.popBackStack()
    }
}
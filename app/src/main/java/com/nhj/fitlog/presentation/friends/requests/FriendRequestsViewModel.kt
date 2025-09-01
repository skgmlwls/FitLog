package com.nhj.fitlog.presentation.friends.requests

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhj.fitlog.FitLogApplication
import com.nhj.fitlog.data.service.FriendRequestItem
import com.nhj.fitlog.data.service.FriendService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val friendService: FriendService
) : ViewModel() {
    val app = context as FitLogApplication
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val requests = mutableStateListOf<FriendRequestItem>()
    private var job: Job? = null

    fun start() {
        val uid = app.userUid ?: return
        if (job != null) return
        job = viewModelScope.launch {
            isLoading.value = true
            friendService.streamIncomingRequests(uid).collectLatest { list ->
                requests.clear(); requests.addAll(list)
                isLoading.value = false; error.value = null
            }
        }
    }

    fun accept(requestId: String, fromUid: String) = viewModelScope.launch {
        val uid = app.userUid ?: return@launch
        try {
            friendService.acceptFriendRequest(uid, requestId, fromUid)
        } catch (e: Exception) { error.value = e.message }
    }

    fun decline(requestId: String) = viewModelScope.launch {
        val uid = app.userUid ?: return@launch
        try {
            friendService.declineFriendRequest(uid, requestId)
        } catch (e: Exception) { error.value = e.message }
    }

    fun back() = app.navHostController.popBackStack()
}
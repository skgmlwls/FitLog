package com.nhj.fitlog.presentation.coach

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import com.nhj.fitlog.FitLogApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/** 액션/하이라이트 구조 */
data class CoachAction(
    val type: String,
    val args: Map<String, Any?> = emptyMap()
)

data class ChatMsg(
    val role: String,                // "user" | "assistant"
    val content: String,
    val highlights: List<String> = emptyList(),
    val actions: List<CoachAction> = emptyList()
)

@HiltViewModel
class CoachViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val application = context as FitLogApplication

    val messages = mutableStateListOf<ChatMsg>()
    val loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val info  = mutableStateOf<String?>(null) // 액션 성공 메시지 등

    // RTDB 구독 핸들들
    private var streamRootRef: DatabaseReference? = null
    private var statusListener: ValueEventListener? = null
    private var contentListener: ValueEventListener? = null
    private var metaListener: ValueEventListener? = null

    private var introSeeded = false

    /** 최초 진입 시, 툴 사용 예시를 어시스턴트 메시지로 시드 */
    fun ensureIntro() {
        if (introSeeded || messages.isNotEmpty()) return
        introSeeded = true

        // 메시지 1: 툴 개요 + 자주 쓰는 질문
        messages.add(
            ChatMsg(
                role = "assistant",
                content = buildString {
                    appendLine("안녕하세요! 저는 FitLog AI 코치예요. 아래처럼 물어보면 필요한 툴들을 자동으로 호출해서 분석해 드려요 🙌")
                    appendLine()
                    appendLine("① 하루/최근 요약")
                    appendLine("• 가장 최근 기록 요약해줘  (최근 기록 찾은 뒤 요약) ")
                    appendLine("• 최근 8회 세션 간단히 보여줘  (날짜/메모/카테고리 볼륨) ")
                    appendLine("• 최근 4주 카테고리 비중 알려줘  (볼륨 비중% + 절대값)")
                    appendLine()
                    appendLine("② 운동별 성능")
                    appendLine("• 랫풀다운 최고 중량 세트 알려줘  (최근 90일)")
                    appendLine("• 랫풀다운 1RM 추세 보여줘  (최근 120일, 주차별)")
                    appendLine()
                    appendLine("③ 운동 종류")
                    appendLine("• 하체 운동 종류 목록 보여줘")
                    appendLine("• '레그' 들어가는 운동 찾아줘")
                    appendLine()
                    appendLine("④ 루틴")
                    appendLine("• 내 루틴 목록 보여줘")
                    appendLine("• '등 전체' 루틴 상세 보여줘")
                    appendLine("• 등·코어 집중 추천 루틴 만들어줘")
                    appendLine("• '등 전체' 루틴 추가해줘: 랫풀다운 3세트, 바벨로우 3세트")
                    appendLine()
                    appendLine("💡 꿀팁")
                    appendLine("• 날짜/운동명/기간을 붙이면 더 정확해요.")
                    appendLine("  예) “8월 20일 기록 요약”, “지난 120일 랫풀다운 1RM”")
                    appendLine("• 다음 주 계획이 필요하면 “다음 주 계획 세워줘(등·코어 집중)”처럼 말해보세요.")
                }
            )
        )
    }

    /** 스트리밍 요청 */
    fun askCoachStreaming(sessionId: String, text: String) = viewModelScope.launch {
        val uid = application.userUid ?: run {
            error.value = "로그인이 필요합니다."
            return@launch
        }

        // 1) 유저 메시지 추가
        messages.add(ChatMsg("user", text))

        // 2) streamId 생성
        val streamId = "stm-${System.currentTimeMillis()}-${UUID.randomUUID()}"

        // 3) RTDB 구독 준비(assistant 버블 미리 하나 추가)
        subscribeStream(uid, sessionId, streamId)

        // 4) Functions 호출 → 서버가 RTDB에 content/meta/status를 밀어줌
        loading.value = true
        error.value = null
        try {
            val payload = hashMapOf(
                "uid" to uid,
                "sessionId" to sessionId,
                "message" to text,
                "streamId" to streamId
            )
            Firebase.functions("asia-northeast3")
                .getHttpsCallable("chatWithCoachStream")
                .call(payload)
                .addOnFailureListener { e ->
                    error.value = (e as? FirebaseFunctionsException)?.message ?: e.message
                }
        } catch (t: Throwable) {
            error.value = t.message ?: t.toString()
            loading.value = false
            unsubscribeStream()
        }
    }

    /** 액션 실행 (예: plan_next_week) */
    fun runAction(action: CoachAction) = viewModelScope.launch {
        val uid = application.userUid ?: run {
            error.value = "로그인이 필요합니다."
            return@launch
        }
        when (action.type) {
            "recommend_routine" -> {
                try {
                    val focusTargets = (action.args["focusTargets"] as? List<*>)?.map { it.toString() } ?: emptyList()
                    val data = hashMapOf(
                        "uid" to uid,
                        "focusTargets" to focusTargets
                    )
                    val res = Firebase.functions("asia-northeast3")
                        .getHttpsCallable("recommend_routine")
                        .call(data)
                        .await()
                    info.value = "추천 루틴을 새로 생성했어요."
                    Log.d("CoachVM", "recommend_routine result=${res.data}")
                } catch (e: Exception) {
                    error.value = e.message ?: "액션 실행 실패"
                }
            }
            "add_routine" -> {
                try {
                    val name = (action.args["name"] ?: "추천 루틴").toString()
                    val memo = (action.args["memo"] ?: "").toString()
                    @Suppress("UNCHECKED_CAST")
                    val exercises = action.args["exercises"] as? List<Map<String, Any?>> ?: emptyList()

                    val data = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "memo" to memo,
                        "exercises" to exercises
                    )
                    val res = Firebase.functions("asia-northeast3")
                        .getHttpsCallable("add_routine")
                        .call(data)
                        .await()
                    info.value = "루틴을 저장했어요."
                    Log.d("CoachVM", "add_routine result=${res.data}")
                } catch (e: Exception) {
                    error.value = e.message ?: "액션 실행 실패"
                }
            }
            "plan_next_week" -> {
                // (기존 분기 유지)
                try {
                    val focusTargets = (action.args["targets"] as? List<*>)?.map { it.toString() } ?: emptyList()
                    val data = hashMapOf(
                        "uid" to uid,
                        "focusTargets" to focusTargets
                    )
                    val res = Firebase.functions("asia-northeast3")
                        .getHttpsCallable("plan_next_week")
                        .call(data)
                        .await()
                    info.value = "다음 주 루틴 초안이 생성됐어요."
                    Log.d("CoachVM", "plan_next_week result=${res.data}")
                } catch (e: Exception) {
                    error.value = e.message ?: "액션 실행 실패"
                }
            }
            else -> {
                info.value = "지원하지 않는 액션입니다: ${action.type}"
            }
        }
    }

    /** RTDB 구독: content/status/meta 세 갈래 */
    private fun subscribeStream(uid: String, sessionId: String, streamId: String) {
        unsubscribeStream()

        val root = FirebaseDatabase.getInstance()
            .getReference("/chatStreams/$uid/$sessionId/$streamId")
        streamRootRef = root

        // 화면에 assistant 버블 하나 추가 (내용은 계속 누적 갱신)
        val assistantIndex = messages.size
        messages.add(ChatMsg(role = "assistant", content = ""))

        // content 전용 listener (가장 잦게 변경)
        val contentRef = root.child("content")
        contentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val content = snapshot.getValue(String::class.java) ?: ""
                if (assistantIndex in messages.indices) {
                    val old = messages[assistantIndex]
                    messages[assistantIndex] = old.copy(content = content)
                }
            }
            override fun onCancelled(error2: DatabaseError) {
                error.value = error2.message
            }
        }
        contentRef.addValueEventListener(contentListener!!)

        // meta(highlights/actions) 전용 listener
        val metaRef = root.child("meta")
        metaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // highlights
                val highlights = mutableListOf<String>()
                snapshot.child("highlights").children.forEach {
                    it.getValue(String::class.java)?.let { s -> highlights.add(s) }
                }
                // actions
                val actions = mutableListOf<CoachAction>()
                snapshot.child("actions").children.forEach { node ->
                    val type = node.child("type").getValue(String::class.java) ?: return@forEach
                    val argsMap = mutableMapOf<String, Any?>()
                    node.child("args").children.forEach { arg ->
                        argsMap[arg.key!!] = arg.value
                    }
                    actions.add(CoachAction(type = type, args = argsMap))
                }

                if (assistantIndex in messages.indices) {
                    val old = messages[assistantIndex]
                    messages[assistantIndex] = old.copy(
                        highlights = highlights,
                        actions = actions
                    )
                }
            }
            override fun onCancelled(error2: DatabaseError) {
                error.value = error2.message
            }
        }
        metaRef.addValueEventListener(metaListener!!)

        // status/error 전용 listener (빈도 적음)
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java) ?: "pending"
                val err = snapshot.child("error").getValue(String::class.java)
                if (!err.isNullOrBlank()) error.value = err
                if (status == "done" || status == "error") {
                    loading.value = false
                    // 정책상 노드 정리는 서버/클라 중 택1. 여기서는 해제만.
                    unsubscribeStream()
                }
            }
            override fun onCancelled(error2: DatabaseError) {
                error.value = error2.message
                loading.value = false
                unsubscribeStream()
            }
        }
        root.addValueEventListener(statusListener!!)
    }

    private fun unsubscribeStream() {
        contentListener?.let { l ->
            streamRootRef?.child("content")?.removeEventListener(l)
        }
        metaListener?.let { l ->
            streamRootRef?.child("meta")?.removeEventListener(l)
        }
        statusListener?.let { l ->
            streamRootRef?.removeEventListener(l)
        }
        contentListener = null
        metaListener = null
        statusListener = null
        streamRootRef = null
    }

    fun onBackNavigation() = application.navHostController.popBackStack()

    override fun onCleared() {
        super.onCleared()
        unsubscribeStream()
    }
}

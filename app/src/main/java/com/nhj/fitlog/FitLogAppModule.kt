package com.nhj.fitlog

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.nhj.fitlog.data.repository.ChatLogsRepository
import com.nhj.fitlog.data.repository.ExerciseRepository
import com.nhj.fitlog.data.repository.FriendRepository
import com.nhj.fitlog.data.repository.RecordRepository
import com.nhj.fitlog.data.repository.RoutineRepository
import com.nhj.fitlog.data.repository.UserRepository
import com.nhj.fitlog.data.service.ChatLogsService
import com.nhj.fitlog.data.service.ExerciseService
import com.nhj.fitlog.data.service.FriendService
import com.nhj.fitlog.data.service.RecordService
import com.nhj.fitlog.data.service.RoutineService
import com.nhj.fitlog.data.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ✅ Hilt 모듈 정의
@Module
@InstallIn(SingletonComponent::class)
object FitLogAppModule {

    // 유저 관련 ////////////////////////////////////
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    @Singleton
    fun provideUserService(
        userRepository: UserRepository
    ): UserService {
        return UserService(userRepository)
    }
    ///////////////////////////////////////////////

    // 운동 관련 ////////////////////////////////////
    @Provides
    @Singleton
    fun provideExerciseRepository(): ExerciseRepository {
        return ExerciseRepository()
    }

    @Provides
    @Singleton
    fun provideExerciseService(
        exerciseRepository: ExerciseRepository
    ): ExerciseService {
        return ExerciseService(exerciseRepository)
    }
    ///////////////////////////////////////////////

    // 루틴 관련 ////////////////////////////////////
    @Provides @Singleton
    fun provideRoutineRepository(): RoutineRepository {
        return RoutineRepository()
    }

    @Provides @Singleton
    fun provideRoutineService(
        routineRepository: RoutineRepository
    ): RoutineService {
        return RoutineService(routineRepository)
    }
    ///////////////////////////////////////////////

    // 운동 기록 ////////////////////////////////////
    @Provides @Singleton
    fun provideRecordRepository(@ApplicationContext ctx: Context) = RecordRepository(ctx.contentResolver)

    @Provides @Singleton
    fun provideRecordService(repo: RecordRepository): RecordService = RecordService(repo)
    ///////////////////////////////////////////////

    // 친구 목록 ////////////////////////////////////
    @Provides @Singleton
    fun provideFriendRepository() = FriendRepository()

    @Provides @Singleton
    fun provideFriendService(repo: FriendRepository) : FriendService = FriendService(repo)
    ///////////////////////////////////////////////


    /** FirebaseAuth 주입 */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    /** FirebaseFunctions (서울 리전 권장: asia-northeast3) */
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions =
        Firebase.functions("asia-northeast3")

    /** Firestore */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    // 채팅 ////////////////////////////////////////
    @Provides @Singleton
    fun provideChatLogsRepository() = ChatLogsRepository()

    @Provides @Singleton
    fun provideChatLogsService(repo: ChatLogsRepository) = ChatLogsService(repo)
    ///////////////////////////////////////////////
}
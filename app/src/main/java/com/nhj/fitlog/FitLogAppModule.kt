package com.nhj.fitlog

import android.content.Context
import com.nhj.fitlog.data.repository.ExerciseRepository
import com.nhj.fitlog.data.repository.FriendRepository
import com.nhj.fitlog.data.repository.RecordRepository
import com.nhj.fitlog.data.repository.RoutineRepository
import com.nhj.fitlog.data.repository.UserRepository
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

}
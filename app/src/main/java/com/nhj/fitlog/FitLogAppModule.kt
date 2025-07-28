package com.nhj.fitlog

import com.nhj.fitlog.data.repository.UserRepository
import com.nhj.fitlog.data.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ✅ Hilt 모듈 정의
@Module
@InstallIn(SingletonComponent::class)
object FitLogAppModule {

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

    // 필요한 다른 Service, Repository도 여기에 추가하면 돼
}
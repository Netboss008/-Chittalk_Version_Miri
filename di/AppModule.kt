package de.chittalk.messenger.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.chittalk.messenger.data.repository.*
import de.chittalk.messenger.data.repository.impl.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideChatRepository(): ChatRepository {
        return ChatRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideStreamRepository(): StreamRepository {
        return StreamRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository {
        return ProfileRepositoryImpl()
    }
}
package com.example.chittalk.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.session.Session
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatrixModule {

    @Provides
    @Singleton
    fun provideMatrixConfiguration(): MatrixConfiguration {
        return MatrixConfiguration(
            applicationFlavor = "Chittalk",
            roomDisplayNameFallbackProvider = null
        )
    }

    @Provides
    @Singleton
    fun provideMatrix(
        @ApplicationContext context: Context,
        configuration: MatrixConfiguration
    ): Matrix {
        return Matrix(context, configuration)
    }

    @Provides
    @Singleton
    fun provideAuthenticationService(matrix: Matrix): AuthenticationService {
        return matrix.authenticationService()
    }
}
package com.jeiel.contextactionassistant.core.di

import com.jeiel.contextactionassistant.ai.MockVisionAnalyzer
import com.jeiel.contextactionassistant.ai.VisionAnalyzer
import com.jeiel.contextactionassistant.data.datastore.SettingsRepository
import com.jeiel.contextactionassistant.data.datastore.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideVisionAnalyzer(): VisionAnalyzer = MockVisionAnalyzer()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

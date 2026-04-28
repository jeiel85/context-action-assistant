package com.jeiel.contextactionassistant.core.di

import com.jeiel.contextactionassistant.ai.GeminiVisionAnalyzer
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
import okhttp3.OkHttpClient

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
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideVisionAnalyzer(
        geminiVisionAnalyzer: GeminiVisionAnalyzer
    ): VisionAnalyzer = object : VisionAnalyzer {
        private val fallback = MockVisionAnalyzer()
        override suspend fun analyze(request: com.jeiel.contextactionassistant.domain.model.AnalysisRequest) =
            geminiVisionAnalyzer.analyze(request).recoverCatching {
                fallback.analyze(request).getOrThrow()
            }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

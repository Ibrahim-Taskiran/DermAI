package com.ibrahim.dermai.di

import com.ibrahim.dermai.data.repository.AnalysisRepository
import com.ibrahim.dermai.data.repository.MockAnalysisRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Uygulama genelindeki bağımlılıkları sağlayan Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Şu an MockAnalysisRepository kullanıyoruz (Demo sürümü).
     *
     * TODO(BACKEND): Backend hazır olduğunda aşağıdaki satırı yoruma al,
     *   MockAnalysisRepository'i kaldır ve ApiAnalysisRepository'i inject et:
     *   return ApiAnalysisRepository(apiService)
     */
    @Provides
    @Singleton
    fun provideAnalysisRepository(): AnalysisRepository {
        return MockAnalysisRepository()
    }
}

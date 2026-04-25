package com.ibrahim.dermai.di

import android.content.Context
import com.ibrahim.dermai.data.remote.DermAIApiService
import com.ibrahim.dermai.data.repository.AnalysisRepository
import com.ibrahim.dermai.data.repository.ApiAnalysisRepository
import com.ibrahim.dermai.data.repository.TrackerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Uygulama genelindeki bağımlılıkları sağlayan Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Backend hazır olduğu için artık gerçek API kullanılıyor.
    // Context, Android 13+ Photo Picker URI'larını ContentResolver ile okumak için gerekli.
    @Provides
    @Singleton
    fun provideAnalysisRepository(
        @ApplicationContext context: Context,
        apiService: DermAIApiService
    ): AnalysisRepository {
        return ApiAnalysisRepository(context, apiService)
    }

    /**
     * Hastalık takip günlüğü için yerel depolama repository'si.
     */
    @Provides
    @Singleton
    fun provideTrackerRepository(@ApplicationContext context: Context): TrackerRepository {
        return TrackerRepository(context)
    }
}

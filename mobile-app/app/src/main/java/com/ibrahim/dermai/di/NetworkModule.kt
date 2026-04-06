package com.ibrahim.dermai.di

import com.ibrahim.dermai.data.remote.DermAIApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Retrofit ve OkHttp network bağımlılıklarını sağlayan Hilt modülü.
 *
 * TODO(BACKEND): Kerem'in FastAPI sunucusu hazır olduğunda:
 *   1. BASE_URL'yi gerçek sunucu adresiyle değiştir
 *   2. AppModule'deki provideAnalysisRepository'i güncelle (ApiAnalysisRepository kullan)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * TODO(BACKEND): Kerem'in API adresini buraya yaz.
     * Örnek: "http://192.168.1.100:8000/" (local geliştirme için)
     * Örnek: "https://api.dermai.com/" (production için)
     */
    private const val BASE_URL = "http://localhost:8000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDermAIApiService(retrofit: Retrofit): DermAIApiService {
        return retrofit.create(DermAIApiService::class.java)
    }
}

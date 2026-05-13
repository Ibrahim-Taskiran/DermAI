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
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Bilgisayarın WiFi IP adresi - telefon ve bilgisayar aynı ağda olmalı
    // IP değişirse burası güncellenmeli (ipconfig ile kontrol et)
    private const val BASE_URL = "http://192.168.238.188:8000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (com.ibrahim.dermai.BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
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

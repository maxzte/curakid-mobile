package com.misterio.core.network

import com.misterio.core.network.api.AiApi
import com.misterio.core.network.api.AssessmentApi
import com.misterio.core.network.api.AuthApi
import com.misterio.core.network.api.ChildApi
import com.misterio.core.network.api.EventApi
import com.misterio.core.network.api.MeasurementApi
import com.misterio.core.network.api.DevelopmentApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.5:8080/"

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideAuthOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("AuthClient") okHttpClient: OkHttpClient): AuthApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
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
    fun provideChildApi(retrofit: Retrofit): ChildApi {
        return retrofit.create(ChildApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMeasurementApi(retrofit: Retrofit): MeasurementApi {
        return retrofit.create(MeasurementApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi {
        return retrofit.create(AiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAssessmentApi(retrofit: Retrofit): AssessmentApi {
        return retrofit.create(AssessmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizApi(retrofit: Retrofit): DevelopmentApi {
        return retrofit.create(DevelopmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi {
        return retrofit.create(EventApi::class.java)
    }
}

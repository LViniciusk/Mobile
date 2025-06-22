package com.example.mygamelist.di

import android.content.Context
import com.example.mygamelist.data.api.RawgService
import com.example.mygamelist.data.repository.GameRepository
import com.example.mygamelist.data.repository.GameRepositoryImpl
import com.example.mygamelist.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.rawg.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRawgService(retrofit: Retrofit): RawgService {
        return retrofit.create(RawgService::class.java)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        rawgService: RawgService
    ): GameRepository {
        val RAWG_API_KEY = "84f487f7f7ce4190a0911d867db3c1ef"
        return GameRepositoryImpl(rawgService, RAWG_API_KEY)
    }
}
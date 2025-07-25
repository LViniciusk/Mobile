package com.example.mygamelist.di

import android.content.Context
import androidx.room.Room
import com.example.mygamelist.data.api.RawgService
import com.example.mygamelist.data.dao.GameDao
import com.example.mygamelist.data.database.AppDatabase
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
import javax.inject.Named
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.example.mygamelist.data.repository.AuthRepository
import com.example.mygamelist.data.repository.AuthRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mygamelist.data.repository.UserRepository
import com.example.mygamelist.data.repository.UserRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.example.mygamelist.data.api.ImgurService
import com.example.mygamelist.data.model.GameCacheTypeConverter
import com.example.mygamelist.data.model.GameStatusConverter
import com.example.mygamelist.data.repository.ImgurRepository
import com.example.mygamelist.data.repository.ImgurRepositoryImpl
import com.example.mygamelist.data.repository.NotificationRepository
import com.example.mygamelist.data.repository.NotificationRepositoryImpl

private const val RAWG_API_KEY_VALUE = "84f487f7f7ce4190a0911d867db3c1ef"
private const val IMGUR_CLIENT_ID = "2595b30a05bc570"

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
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.rawg.io/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("ImgurRetrofit")
    fun provideImgurRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.imgur.com/")
            .client(okHttpClient)
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
    fun provideImgurService(@Named("ImgurRetrofit") retrofit: Retrofit): ImgurService {
        return retrofit.create(ImgurService::class.java)
    }

    @Provides
    @Singleton
    fun provideImgurRepository(
        imgurService: ImgurService,
        @Named("imgur_client_id") imgurClientId: String,
        @ApplicationContext context: Context
    ): ImgurRepository {
        return ImgurRepositoryImpl(imgurService, imgurClientId, context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        rawgService: RawgService,
        gameDao: GameDao,
        @Named("rawg_api_key") apiKey: String,
        firestore: FirebaseFirestore
    ): GameRepository {
        return GameRepositoryImpl(rawgService, gameDao, apiKey, firestore)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        gameStatusConverter: GameStatusConverter,
        gameCacheTypeConverter: GameCacheTypeConverter
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my-game-list-db"
        )
            .addTypeConverter(gameStatusConverter)
            .addTypeConverter(gameCacheTypeConverter)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    @Named("rawg_api_key")
    fun provideRawgApiKey(): String {
        return RAWG_API_KEY_VALUE
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth, userRepository: UserRepository): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, userRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): UserRepository {
        return UserRepositoryImpl(firestore)
    }

    @Provides
    @Named("imgur_client_id")
    fun provideImgurClientId(): String {
        return IMGUR_CLIENT_ID
    }
}
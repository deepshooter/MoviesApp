package com.deepshooter.moviesapp.di


import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.deepshooter.moviesapp.BuildConfig
import com.deepshooter.moviesapp.data.local.room.UserDatabase
import com.deepshooter.moviesapp.data.network.ApiKeyInterceptor
import com.deepshooter.moviesapp.data.network.MoviesApiService
import com.deepshooter.moviesapp.data.repository.LocalUserRepository
import com.deepshooter.moviesapp.data.repository.NetworkMoviesRepository
import com.deepshooter.moviesapp.domain.repository.MoviesRepository
import com.deepshooter.moviesapp.domain.repository.UserRepository
import com.deepshooter.moviesapp.util.MoviesConstant.BASE_URL
import com.deepshooter.moviesapp.util.MoviesConstant.SETTING_DATASTORE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTING_DATASTORE)


    @Provides
    @Singleton
    fun provideMoviesApiService(): MoviesApiService {

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.API_KEY))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoviesApiService::class.java)

    }

    @Provides
    @Singleton
    fun provideMoviesRepository(moviesApiService: MoviesApiService): MoviesRepository =
        NetworkMoviesRepository(moviesApiService)


    @Provides
    @Singleton
    fun provideUserRepository(context: Application): UserRepository = LocalUserRepository(
        UserDatabase.getDatabase(context).userDao(),
        context.settingsDataStore
    )

}
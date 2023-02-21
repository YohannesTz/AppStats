package com.github.yohannes.appstats.di

import android.app.Application
import com.github.yohannes.appstats.data.repository.AppUsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // TODO

    @Provides
    @Singleton
    fun provideAppUsageRepository(application: Application): AppUsageRepository {
        return AppUsageRepository(application)
    }

}
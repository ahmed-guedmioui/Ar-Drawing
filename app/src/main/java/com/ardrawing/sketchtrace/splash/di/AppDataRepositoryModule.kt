package com.ardrawing.sketchtrace.splash.di

import com.ardrawing.sketchtrace.splash.data.repository.AppDataRepositoryImpl
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Ahmed Guedmioui
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppDataRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppDataRepository(
        appDataRepositoryImpl: AppDataRepositoryImpl
    ): AppDataRepository

}













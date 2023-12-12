package com.med.drawing.core.di

import com.med.drawing.core.data.repository.AppDataRepositoryImpl
import com.med.drawing.core.domain.repository.AppDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppDataRepository(
        appDataRepositoryImpl: AppDataRepositoryImpl
    ): AppDataRepository

}













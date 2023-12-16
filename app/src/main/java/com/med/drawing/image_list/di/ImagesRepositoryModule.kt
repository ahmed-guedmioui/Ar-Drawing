package com.med.drawing.image_list.di

import com.med.drawing.image_list.data.repository.ImagesRepositoryImpl
import com.med.drawing.image_list.domain.repository.ImagesRepository
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
abstract class ImagesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImagesRepository(
        imagesRepositoryImpl: ImagesRepositoryImpl
    ): ImagesRepository

}













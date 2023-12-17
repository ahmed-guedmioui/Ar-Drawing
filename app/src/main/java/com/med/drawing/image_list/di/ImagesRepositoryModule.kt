package com.med.drawing.image_list.di

import com.med.drawing.image_list.data.repository.ImageCategoriesRepositoryImpl
import com.med.drawing.image_list.domain.repository.ImageCategoriesRepository
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
        imageCategoriesRepositoryImpl: ImageCategoriesRepositoryImpl
    ): ImageCategoriesRepository

}













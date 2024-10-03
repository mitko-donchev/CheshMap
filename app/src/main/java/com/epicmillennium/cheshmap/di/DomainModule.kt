package com.epicmillennium.cheshmap.di

import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import com.epicmillennium.cheshmap.domain.usecase.AddAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.DeleteAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.DeleteWaterSourceByIdUseCase
import com.epicmillennium.cheshmap.domain.usecase.GetAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.GetWaterSourceByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun provideAddAllWaterSourcesUseCase(waterSourcesRepository: WaterSourcesRepository) =
        AddAllWaterSourcesUseCase(waterSourcesRepository)

    @Provides
    fun provideGetAllWaterSourcesUseCase(waterSourcesRepository: WaterSourcesRepository) =
        GetAllWaterSourcesUseCase(waterSourcesRepository)

    @Provides
    fun provideGetWaterSourceByIdUseCase(waterSourcesRepository: WaterSourcesRepository) =
        GetWaterSourceByIdUseCase(waterSourcesRepository)

    @Provides
    fun provideDeleteWaterSourceByIdUseCase(waterSourcesRepository: WaterSourcesRepository) =
        DeleteWaterSourceByIdUseCase(waterSourcesRepository)

    @Provides
    fun provideDeleteAllWaterSourcesUseCase(waterSourcesRepository: WaterSourcesRepository) =
        DeleteAllWaterSourcesUseCase(waterSourcesRepository)
}
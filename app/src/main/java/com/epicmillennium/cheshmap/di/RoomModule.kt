package com.epicmillennium.cheshmap.di

import android.content.Context
import androidx.room.Room
import com.epicmillennium.cheshmap.data.repository.watersources.WaterSourcesRepositoryImpl
import com.epicmillennium.cheshmap.data.repository.watersources.WaterSourcesTable
import com.epicmillennium.cheshmap.data.source.room.WaterSourcesDatabase
import com.epicmillennium.cheshmap.data.source.room.WaterSourcesTableImpl
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val WATER_SOURCES_DB = "water_sources_db"

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideWaterSourcesRepository(waterSourcesTable: WaterSourcesTable): WaterSourcesRepository =
        WaterSourcesRepositoryImpl(waterSourcesTable)

    @Provides
    @Singleton
    fun provideWaterSourcesTable(waterSourcesDatabase: WaterSourcesDatabase): WaterSourcesTable =
        WaterSourcesTableImpl(waterSourcesDatabase)
}

@Module
@InstallIn(SingletonComponent::class)
object SourceModule {
    @Provides
    @Singleton
    fun provideWaterSourcesTable(@ApplicationContext appContext: Context): WaterSourcesDatabase =
        Room.databaseBuilder(appContext, WaterSourcesDatabase::class.java, WATER_SOURCES_DB)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideWaterSourcesDao(waterSourcesDatabase: WaterSourcesDatabase) =
        waterSourcesDatabase.waterSourcesDao()
}
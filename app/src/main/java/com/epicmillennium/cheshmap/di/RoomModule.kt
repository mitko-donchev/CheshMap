package com.epicmillennium.cheshmap.di

import android.content.Context
import androidx.room.Room
import com.epicmillennium.cheshmap.data.repository.firebase.AccountServiceImpl
import com.epicmillennium.cheshmap.data.repository.user.UserRepositoryImpl
import com.epicmillennium.cheshmap.data.repository.user.UserTable
import com.epicmillennium.cheshmap.data.repository.watersources.WaterSourcesRepositoryImpl
import com.epicmillennium.cheshmap.data.repository.watersources.WaterSourcesTable
import com.epicmillennium.cheshmap.data.source.room.db.UserDatabase
import com.epicmillennium.cheshmap.data.source.room.db.UserTableImpl
import com.epicmillennium.cheshmap.data.source.room.db.WaterSourcesDatabase
import com.epicmillennium.cheshmap.data.source.room.db.WaterSourcesTableImpl
import com.epicmillennium.cheshmap.domain.auth.AccountService
import com.epicmillennium.cheshmap.domain.auth.UserRepository
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_DB = "user_db"
private const val WATER_SOURCES_DB = "water_sources_db"

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // User
    @Provides
    @Singleton
    fun provideUserAuthRepository(auth: FirebaseAuth): AccountService = AccountServiceImpl(auth)

    @Provides
    @Singleton
    fun provideUserRepository(userTable: UserTable): UserRepository = UserRepositoryImpl(userTable)

    @Provides
    @Singleton
    fun provideUserTableImpl(userDatabase: UserDatabase): UserTable = UserTableImpl(userDatabase)

    // Water source
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

    // User
    @Provides
    @Singleton
    fun provideUserTable(@ApplicationContext appContext: Context): UserDatabase =
        Room.databaseBuilder(appContext, UserDatabase::class.java, USER_DB)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideUserDao(userDatabase: UserDatabase) = userDatabase.userDao()

    // Water source
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
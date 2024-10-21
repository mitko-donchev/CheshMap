package com.epicmillennium.cheshmap.di

import com.epicmillennium.cheshmap.domain.auth.AccountService
import com.epicmillennium.cheshmap.domain.auth.UserRepository
import com.epicmillennium.cheshmap.domain.marker.WaterSourcesRepository
import com.epicmillennium.cheshmap.domain.usecase.auth.DeleteAccountUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.GetCurrentUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.HasUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SendRecoveryEmailUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignInWithEmailUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignInWithGoogleUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignOutUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignUpWithEmailUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.DeleteUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.GetUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.user.InsertUserUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.AddAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.AddWaterSourceUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.DeleteAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.DeleteWaterSourceByIdUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.GetAllWaterSourcesUseCase
import com.epicmillennium.cheshmap.domain.usecase.watersource.GetWaterSourceByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    // User Auth use cases
    @Provides
    fun provideHasUserUseCase(accountService: AccountService) = HasUserUseCase(accountService)

    @Provides
    fun provideGetCurrentUserUseCase(accountService: AccountService) =
        GetCurrentUserUseCase(accountService)

    @Provides
    fun provideSignInWithEmailUseCase(accountService: AccountService) =
        SignInWithEmailUseCase(accountService)

    @Provides
    fun provideSignUpWithGoogleUseCase(accountService: AccountService) =
        SignInWithGoogleUseCase(accountService)

    @Provides
    fun provideSignUpWithEmailUseCase(accountService: AccountService) =
        SignUpWithEmailUseCase(accountService)

    @Provides
    fun provideSendRecoveryEmailUseCase(accountService: AccountService) =
        SendRecoveryEmailUseCase(accountService)

    @Provides
    fun provideSignOutUseCase(accountService: AccountService) = SignOutUseCase(accountService)

    @Provides
    fun provideDeleteAccountUseCase(accountService: AccountService) =
        DeleteAccountUseCase(accountService)

    // User DB use cases
    @Provides
    fun provideInsertUserUseCase(userRepository: UserRepository) = InsertUserUseCase(userRepository)

    @Provides
    fun provideGetUserUseCase(userRepository: UserRepository) = GetUserUseCase(userRepository)

    @Provides
    fun provideDeleteUserUseCase(userRepository: UserRepository) = DeleteUserUseCase(userRepository)

    // Water source use cases
    @Provides
    fun provideAddWaterSourceUseCase(waterSourcesRepository: WaterSourcesRepository) =
        AddWaterSourceUseCase(waterSourcesRepository)

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
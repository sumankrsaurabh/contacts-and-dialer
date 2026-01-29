package com.coderon.phone.di

import android.content.ContentResolver
import com.coderon.phone.data.AppDatabase
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.CallLogRepositoryImpl
import com.coderon.phone.data.repository.ContactRepositoryImpl
import com.coderon.phone.data.repository.SettingsRepository
import com.coderon.phone.data.repository.VoicemailRepository
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import com.coderon.phone.domain.usecase.DeleteCallLogUseCase
import com.coderon.phone.domain.usecase.GetContactsUseCase
import com.coderon.phone.domain.usecase.ObserveCallLogsUseCase
import com.coderon.phone.domain.usecase.SaveContactUseCase
import com.coderon.phone.domain.usecase.UpdateContactUseCase
import com.coderon.phone.utils.VoicemailRecorder
import com.coderon.phone.viewmodel.BlockedNumbersViewModel
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import com.coderon.phone.viewmodel.SettingsViewModel
import com.coderon.phone.viewmodel.VoicemailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Database and DAOs
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().blockedNumberDao() }
    single { get<AppDatabase>().voicemailDao() }

    // Provide ContentResolver from the Android context
    single<ContentResolver> { androidContext().contentResolver }

    // Repository injections using modern DSL
    singleOf(::ContactRepositoryImpl) bind ContactRepository::class
    singleOf(::SettingsRepository)
    singleOf(::CallLogRepositoryImpl) bind CallLogRepository::class

    singleOf(::VoicemailRecorder)
    singleOf(::BlockedNumberRepository)
    singleOf(::VoicemailRepository)

    // Use Case injections
    factoryOf(::GetContactsUseCase)
    factoryOf(::SaveContactUseCase)
    factoryOf(::UpdateContactUseCase)
    factoryOf(::ObserveCallLogsUseCase)
    factoryOf(::DeleteCallLogUseCase)

    // ViewModel injections
    viewModelOf(::ContactViewModel)
    viewModelOf(::CallLogViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::BlockedNumbersViewModel)
    viewModelOf(::VoicemailViewModel)
}

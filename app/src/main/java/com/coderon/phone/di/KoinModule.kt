package com.coderon.phone.di

import android.content.ContentResolver
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.CallLogRepositoryImpl
import com.coderon.phone.data.repository.ContactRepositoryImpl
import com.coderon.phone.data.repository.VoicemailRepository
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import com.coderon.phone.domain.usecase.DeleteCallLogUseCase
import com.coderon.phone.domain.usecase.GetContactsUseCase
import com.coderon.phone.domain.usecase.ObserveCallLogsUseCase
import com.coderon.phone.domain.usecase.SaveContactUseCase
import com.coderon.phone.domain.usecase.UpdateContactUseCase
import com.coderon.phone.utils.VoicemailRecorder
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Provide ContentResolver from the Android context
    single<ContentResolver> { androidContext().contentResolver }

    // Repository injections using modern DSL
    singleOf(::ContactRepositoryImpl) bind ContactRepository::class
    single<CallLogRepository> {
        CallLogRepositoryImpl(
            contentResolver = get(),
            context = androidContext()
        )
    }
    singleOf(::VoicemailRecorder)
    singleOf(::BlockedNumberRepository)
    singleOf(::VoicemailRepository)

    // Use Case injections
    factoryOf(::GetContactsUseCase)
    factoryOf(::SaveContactUseCase)
    factoryOf(::UpdateContactUseCase)
    factoryOf(::ObserveCallLogsUseCase)
    factoryOf(::DeleteCallLogUseCase)

    // ViewModel injections using modern DSL (removes deprecation)
    viewModelOf(::ContactViewModel)
    viewModelOf(::CallLogViewModel)
}

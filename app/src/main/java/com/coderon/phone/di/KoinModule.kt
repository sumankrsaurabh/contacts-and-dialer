package com.coderon.phone.di

import android.content.ContentResolver
import com.coderon.phone.call.CallManager
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.CallLogRepositoryImpl
import com.coderon.phone.data.repository.ContactRepositoryImpl
import com.coderon.phone.data.repository.VoicemailRepository
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import com.coderon.phone.utils.VoicemailRecorder
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.CallViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Provide ContentResolver from the Android context
    single<ContentResolver> { androidContext().contentResolver }

    // Repository injections
    single<ContactRepository> { ContactRepositoryImpl(contentResolver = get()) }
    single<CallLogRepository> { CallLogRepositoryImpl(contentResolver = get()) }
    single { VoicemailRecorder() }
    single { BlockedNumberRepository(blockedNumberDao = get()) }
    single { VoicemailRepository(voicemailDao = get()) }
    single { VoicemailRecorder() }

    single {
        CallManager(
        )
    }
    // ViewModel injections
    viewModel { ContactViewModel(contactRepository = get()) }
    viewModel { CallLogViewModel(callLogRepository = get()) }
    viewModel { CallViewModel(get()) }
}
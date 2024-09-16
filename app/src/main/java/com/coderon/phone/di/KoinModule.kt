package com.coderon.phone.di

import android.content.ContentResolver
import com.coderon.phone.domain.CallLogRepository
import com.coderon.phone.domain.CallLogRepositoryImpl
import com.coderon.phone.domain.ContactRepository
import com.coderon.phone.domain.ContactRepositoryImpl
import com.coderon.phone.domain.GetCallLogsUseCase
import com.coderon.phone.domain.GetContactsUseCase
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Provide ContentResolver from the Android context
    single<ContentResolver> { androidContext().contentResolver }

    // Repository injections
    single<ContactRepository> { ContactRepositoryImpl(get()) } // pass ContentResolver
    single<CallLogRepository> { CallLogRepositoryImpl(get()) } // pass ContentResolver

    // Use case injections
    factory { GetContactsUseCase(get()) }
    factory { GetCallLogsUseCase(get()) }

    // ViewModel injections
    viewModel { ContactViewModel(get()) }
    viewModel { CallLogViewModel(get()) }
}

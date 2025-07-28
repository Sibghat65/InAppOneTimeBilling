package com.sibghat.test.inapp.onetime.di


import com.sibghat.test.inapp.onetime.PremiumViewModel
import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManager
import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManagerImp
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    viewModel { PremiumViewModel(get(),get(),get(),get(),get(),get()) }
    single<InAppOneTimeManager> {
        InAppOneTimeManagerImp(androidContext())
    }
}

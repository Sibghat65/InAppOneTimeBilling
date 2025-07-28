package com.sibghat.test.inapp.onetime.di


import com.sibghat.test.inapp.onetime.domain.usecases.ConsumeOneTimePurchaseUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.EndBillingConnectionUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.PurchaseProductUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.QueryProductDetailsUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.QueryProductPurchasedUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.StartBillingConnectionUseCase
import org.koin.dsl.module


val useCaseModule = module {

    // Use case for consuming a previously purchased one-time product
    // (e.g., after it's been granted to the user)
    factory {
        ConsumeOneTimePurchaseUseCase(get())
    }

    // Use case to start and establish a BillingClient connection
    factory {
        StartBillingConnectionUseCase(get())
    }

    // Use case to safely close and clean up the BillingClient connection
    factory {
        EndBillingConnectionUseCase(get())
    }

    // Use case to launch the purchase flow for a selected product
    factory {
        PurchaseProductUseCase(get())
    }

    // Use case to query details (like price, title) of available products
    factory {
        QueryProductDetailsUseCase(get())
    }

    // Use case to check whether a specific product has already been purchased
    factory {
        QueryProductPurchasedUseCase(get())
    }
}

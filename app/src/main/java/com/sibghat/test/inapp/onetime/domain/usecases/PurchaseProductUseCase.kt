package com.sibghat.test.inapp.onetime.domain.usecases

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManager

class PurchaseProductUseCase(private val inAppOneTimeManager: InAppOneTimeManager) {
    suspend fun execute(activity: Activity, productDetails: ProductDetails) =
        inAppOneTimeManager.purchaseProductFlow(activity, productDetails)
}
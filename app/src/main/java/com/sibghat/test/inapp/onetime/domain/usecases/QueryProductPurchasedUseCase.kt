package com.sibghat.test.inapp.onetime.domain.usecases

import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManager

class QueryProductPurchasedUseCase(private val inAppOneTimeManager: InAppOneTimeManager) {
    suspend fun execute(productKey: String) = inAppOneTimeManager.queryProductPurchased(productKey)
}
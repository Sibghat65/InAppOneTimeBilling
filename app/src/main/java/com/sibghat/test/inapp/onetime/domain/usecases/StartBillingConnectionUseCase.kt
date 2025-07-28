package com.sibghat.test.inapp.onetime.domain.usecases

import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManager

class StartBillingConnectionUseCase(private val inAppOneTimeManager: InAppOneTimeManager) {
    suspend fun execute() = inAppOneTimeManager.billingConnectFlow()
}
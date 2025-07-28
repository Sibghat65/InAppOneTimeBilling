package com.sibghat.test.inapp.onetime.domain.usecases

import com.sibghat.test.inapp.onetime.in_app_purchases.InAppOneTimeManager

class QueryProductDetailsUseCase(private val inAppOneTimeManager: InAppOneTimeManager) {
   suspend fun execute(skuList:List<String>) = inAppOneTimeManager.queryProducts(skuList)
}
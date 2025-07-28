package com.sibghat.test.inapp.onetime.in_app_purchases

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.Flow

interface InAppOneTimeManager {
    suspend fun billingConnectFlow(): Flow<Boolean>
    suspend fun queryProductPurchased(productKey: String): Flow<Boolean>
    suspend fun queryProducts(skuList: List<String>): Flow<ProductDetails>
    suspend fun purchaseProductFlow(activity: Activity,productDetails: ProductDetails):Flow<Boolean>
    suspend fun consumeTestProduct():Flow<Boolean>
    suspend fun endBillingConnection()
}
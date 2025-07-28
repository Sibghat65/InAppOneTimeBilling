package com.sibghat.test.inapp.onetime.in_app_purchases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
    Note: Please use context.applicationContext to be passed to this class to avoid memory leaks instead of passing
    activity or fragment context, in my case I am using koin di which will use applicationContext()
*/

class InAppOneTimeManagerImp(private val context: Context) : PurchasesUpdatedListener,
    InAppOneTimeManager {
    companion object {
        var LOG_TAG = "inAppLogsTag"
    }

    private var retryCount = 0
    private val maxRetries = 3
    private var purchaseToken = ""
    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams
                .newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    @Volatile
    private var purchaseCallback: ((Boolean, Purchase?) -> Unit)? = null

    /**
     * Starts connection to the Google Play BillingClient.
     * Automatically tries to reconnect if the service is disconnected.
     *
     * @param onConnected Optional callback invoked when BillingClient is successfully connected.
     */
    private fun startConnection(onConnected: (() -> Unit)? = null) {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(LOG_TAG, "Connected to BillingClient")
                    retryCount = 0 // Reset retry count on successful connection
                    onConnected?.invoke()
                } else {
                    Log.e(LOG_TAG, "Billing setup failed with code: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(LOG_TAG, "BillingClient disconnected. Attempting to reconnect...")

                /*
                 * Reconnection is necessary because:
                 * - BillingClient is a bound service which may get disconnected:
                 *   - When app goes to background
                 *   - Due to temporary service disruptions
                 *   - In low memory situations, etc.
                 *
                 * Best practice:
                 * - Limit retry attempts to avoid infinite loops or ANRs
                 * - Use exponential backoff between retries
                 */
                retryCount++
                if (retryCount <= maxRetries) {
                    val delayMillis = 2000L * retryCount // Backoff: 2s, 4s, 6s...
                    Log.d(
                        LOG_TAG,
                        "Reconnecting in ${delayMillis}ms (attempt $retryCount/$maxRetries)"
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        startConnection(onConnected)
                    }, delayMillis)
                } else {
                    Log.e(LOG_TAG, "Failed to reconnect after $maxRetries attempts.")
                    // Optionally notify user or report failure here
                }
            }
        })
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.d(LOG_TAG, "InAppPurchaseHelper_: onPurchasesUpdated() called")

        // Check if purchase is successful
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.d(LOG_TAG, "InAppPurchaseHelper_: purchase success, count=${purchases.size}")
            purchases.forEach { purchase ->
                handlePurchase(purchase)
            }
        } else {
            Log.w(
                LOG_TAG,
                "InAppPurchaseHelper_: purchase failed - code=${billingResult.responseCode}, msg=${billingResult.debugMessage}"
            )
            purchaseCallback?.invoke(false, null)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Log.d(LOG_TAG, "InAppPurchaseHelper_: handlePurchase() - token=${purchase.purchaseToken}")
        purchaseToken = purchase.purchaseToken
        // Check if purchase state is PURCHASED
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                Log.d(LOG_TAG, "InAppPurchaseHelper_: acknowledging purchase...")

                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgeParams) { billingResult ->
                    val success = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    if (success) {
                        Log.d(LOG_TAG, "InAppPurchaseHelper_: purchase acknowledged successfully")
                        purchaseCallback?.invoke(true, purchase)
                    } else {
                        Log.e(
                            LOG_TAG,
                            "InAppPurchaseHelper_: acknowledge failed - code=${billingResult.responseCode}, msg=${billingResult.debugMessage}"
                        )
                        purchaseCallback?.invoke(false, null)
                    }
                }
            } else {
                Log.d(LOG_TAG, "InAppPurchaseHelper_: purchase already acknowledged")
                purchaseCallback?.invoke(true, purchase)
            }
        } else {
            Log.w(
                LOG_TAG,
                "InAppPurchaseHelper_: purchase not completed (state=${purchase.purchaseState})"
            )
            purchaseCallback?.invoke(false, null)
        }
    }

    override suspend fun billingConnectFlow(): Flow<Boolean> = callbackFlow {
        startConnection {
            trySend(true)
            close()
        }
        awaitClose()
    }

    override suspend fun queryProductPurchased(productKey: String): Flow<Boolean> = callbackFlow {
        queryPurchases(productKey) {
            trySend(it)
            close()
        }
        awaitClose {}
    }

    override suspend fun queryProducts(skuList: List<String>): Flow<ProductDetails> =
        callbackFlow {
            queryProducts(skuList) {
                if (!it.isNullOrEmpty()) {
                    trySend(it[0])
                    close()
                }
            }
            awaitClose()
        }

    override suspend fun purchaseProductFlow(
        activity: Activity,
        productDetails: ProductDetails
    ): Flow<Boolean> = callbackFlow {
        // Start purchase flow
        launchPurchaseFlow(activity, productDetails) { isSuccess, _ ->
            Log.d(LOG_TAG, "InAppPurchaseHelper_: purchase result returned = $isSuccess")
            trySend(isSuccess) // Emit result
            close()            // Close the flow after response
        }
        awaitClose() // Await closure if needed externally
    }

    private fun launchPurchaseFlow(
        activity: Activity,
        productDetails: ProductDetails,
        callback: (Boolean, Purchase?) -> Unit
    ) {
        Log.d(LOG_TAG, "InAppPurchaseHelper_: launching billing flow")

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)

        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(LOG_TAG, "InAppPurchaseHelper_: billing flow launched")
            purchaseCallback = callback // Assign callback for result handling
        } else {
            Log.w(
                LOG_TAG,
                "InAppPurchaseHelper_: billing flow failed - code=${result.responseCode}, msg=${result.debugMessage}"
            )
            callback(false, null) // Notify failure
        }
    }

    override suspend fun consumeTestProduct(): Flow<Boolean> = callbackFlow {
        consumePurchase(purchaseToken) {
            trySend(it)
            close()
        }
        awaitClose()
    }

    override suspend fun endBillingConnection() {
       billingClient.endConnection()
    }

    private fun queryProducts(skuList: List<String>, callback: (List<ProductDetails>?) -> Unit) {
        Log.d(LOG_TAG, "InAppPurchaseHelper_: queryProducts() called")
        // Build request with list of INAPP products
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                skuList.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            ).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(LOG_TAG, "InAppPurchaseHelper_: Products fetched successfully")
                // Log details of each product
                productDetailsList.productDetailsList.forEach { product ->
                    Log.d(
                        LOG_TAG,
                        "Product: id=${product.productId}, title=${product.title}, price=${product.oneTimePurchaseOfferDetails?.formattedPrice}"
                    )
                }
                callback(productDetailsList.productDetailsList)
            } else {
                Log.e(
                    LOG_TAG,
                    "InAppPurchaseHelper_: Failed to fetch products. Code=${billingResult.responseCode}, Msg=${billingResult.debugMessage}"
                )
                callback(null)
            }
        }
    }

    private fun queryPurchases(productKey: String, isPurchased: (Boolean) -> Unit) {
        Log.d(LOG_TAG, "InAppPurchaseHelper_: queryPurchases() called")
        // Query purchases of type INAPP
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            Log.d(LOG_TAG, "InAppPurchaseHelper_: purchases response received")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Look for a purchase matching the given productKey
                val matchedPurchase = purchases.firstOrNull { it.products.contains(productKey) }

                if (matchedPurchase != null) {
                    Log.d(
                        LOG_TAG,
                        "InAppPurchaseHelper_: match found, token=${matchedPurchase.purchaseToken}"
                    )
                    purchaseToken = matchedPurchase.purchaseToken
                    isPurchased(true)
                } else {
                    Log.d(
                        LOG_TAG,
                        "InAppPurchaseHelper_: no purchase match for productKey=$productKey"
                    )
                    isPurchased(false)
                }
            } else {
                Log.e(
                    LOG_TAG,
                    "InAppPurchaseHelper_: failed to query purchases. Code=${billingResult.responseCode}, Msg=${billingResult.debugMessage}"
                )
                isPurchased(false)
            }
        }
    }
    private fun consumePurchase(purchaseToken: String, callback: (Boolean) -> Unit) {
        // Build the consume params with the purchase token
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        // Call consumeAsync to allow repurchasing the product
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            Log.d(
                LOG_TAG,
                "InAppPurchaseHelper_: consume result - code=${billingResult.responseCode}, msg=${billingResult.debugMessage}"
            )
            // Return whether the consume was successful
            callback(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }


}
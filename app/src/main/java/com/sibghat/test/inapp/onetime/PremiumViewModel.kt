package com.sibghat.test.inapp.onetime

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.sibghat.test.inapp.onetime.domain.usecases.ConsumeOneTimePurchaseUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.EndBillingConnectionUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.PurchaseProductUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.QueryProductDetailsUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.QueryProductPurchasedUseCase
import com.sibghat.test.inapp.onetime.domain.usecases.StartBillingConnectionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val startConnectionUseCase: StartBillingConnectionUseCase,
    private val endConnectionUseCase: EndBillingConnectionUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val queryProductDetailsUseCase: QueryProductDetailsUseCase,
    private val queryProductPurchasedUseCase: QueryProductPurchasedUseCase,
    private val consumeOneTimePurchaseUseCase: ConsumeOneTimePurchaseUseCase
) : ViewModel() {

    // Holds product details for UI to display
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails

    // Indicates whether the product has already been purchased
    private val _isPurchased = MutableStateFlow(false)
    val isPurchased: StateFlow<Boolean> = _isPurchased

    // Emits true when a purchase is successful (used for triggering UI events)
    private val _purchaseSuccess = MutableSharedFlow<Boolean>()
    val purchaseSuccess: SharedFlow<Boolean> = _purchaseSuccess

    // Starts BillingClient connection and invokes callback with connection status
    fun startBillingConnection(onConnected: (Boolean) -> Unit) {
        viewModelScope.launch {
            startConnectionUseCase.execute().collectLatest { isConnected ->
                onConnected(isConnected)
            }
        }
    }

    // Ends BillingClient connection to release resources
    private fun endBillingConnection() {
        viewModelScope.launch {
            endConnectionUseCase.execute()
        }
    }

    // Queries Play Store for product details using the given product IDs
    fun loadProducts(productIds: List<String>) {
        viewModelScope.launch {
            queryProductDetailsUseCase.execute(productIds).collect { productDetails ->
                _productDetails.value = productDetails
            }
        }
    }

    // Checks if the user has already purchased the given product
    fun checkIfPurchased(productId: String) {
        viewModelScope.launch {
            queryProductPurchasedUseCase.execute(productId).collect { purchased ->
                _isPurchased.value = purchased
            }
        }
    }

    // Launches Google Play Billing purchase flow
    fun startPurchaseProduct(activity: Activity, productDetails: ProductDetails) {
        viewModelScope.launch {
            purchaseProductUseCase.execute(activity, productDetails).collect {
                _purchaseSuccess.emit(it)
            }
        }
    }

    // Used for consuming Google’s test purchase product (e.g., android.test.purchased)
    fun consumeTestProduct() {
        viewModelScope.launch {
            consumeOneTimePurchaseUseCase.execute()
        }
    }

    // Automatically disconnects billing client when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        endBillingConnection()
    }
}

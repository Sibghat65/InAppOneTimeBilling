package com.sibghat.test.inapp.onetime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sibghat.test.inapp.onetime.ui.theme.InAppOneTimeImplementationTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel:PremiumViewModel = koinViewModel()
            val productDetails by viewModel.productDetails.collectAsStateWithLifecycle()
            InAppOneTimeImplementationTheme {
                //first call for start connecting for billing client
                viewModel.startBillingConnection {isConnected->
                    Toast.makeText(this, "Billing Connected: $isConnected", Toast.LENGTH_SHORT).show()
                    //do what you want according to your usecase
                }

                //query if product already purchased or not
                viewModel.checkIfPurchased("product_key")
                //query product details list
                viewModel.loadProducts(listOf("product_key"))
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Button(onClick = {
                            productDetails?.let {
                                //purchase flow
                                viewModel.startPurchaseProduct(this@MainActivity,it)
                            }?:Toast.makeText(this@MainActivity,"no products found",Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = "Go Premium")
                        }
                        Button(onClick = {
                            viewModel.consumeTestProduct()
                        }) {
                            Text(text = "Consume Test Product")
                        }
                    }
                }
            }
        }
    }
}

package com.basilalasadi.iti.kotlin.productsstateflow.ui.view

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.ui.theme.ViewModelSharingTheme
import com.basilalasadi.iti.kotlin.productsstateflow.ui.viewmodel.ProductsViewModel
import kotlinx.coroutines.launch


class FavoritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ViewModelSharingTheme {
                FavoriteProductsListScreen(
                    isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteProductsListScreen(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel = viewModel<ProductsViewModel>(factory = ProductsViewModel.Factory(LocalContext.current))
    val favoriteProducts = viewModel.favoriteProducts.collectAsStateWithLifecycle(listOf())

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val selectedProduct = rememberSaveable { mutableStateOf<FavoritableProduct?>(null) }

    LaunchedEffect(scope) {
        scope.launch {
            viewModel.message.collect {
                if (it.isNotBlank()) {
                    snackbarHostState.currentSnackbarData?.dismiss()

                    launch {
                        snackbarHostState.showSnackbar(it)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { (context as? Activity)?.finish() }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Row(
            modifier = Modifier.padding(contentPadding),
        ) {
            ProductsListContainer(
                products = favoriteProducts.value,
                isLoading = false,
                errorMessage = null,
                reloadProducts = null,
                onProductSelected = { product ->
                    selectedProduct.value = product

                    if (!isLandscape) {
                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                            putExtra(ProductDetailActivity.EXTRA_PRODUCT, product)
                        }
                        context.startActivity(intent)
                    }
                },
                onProductSetFavorite = { product ->
                    viewModel.setFavorite(product.id, product.isFavorite != true)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.5f)
            )
            if (isLandscape && selectedProduct.value != null) {
                ProductDetail(
                    product = selectedProduct.value!!,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                )
            }
        }
    }
}

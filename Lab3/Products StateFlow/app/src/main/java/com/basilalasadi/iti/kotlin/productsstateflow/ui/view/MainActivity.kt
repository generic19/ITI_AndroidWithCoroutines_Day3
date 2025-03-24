package com.basilalasadi.iti.kotlin.productsstateflow.ui.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.data.repository.ProductsRepository.ProductsState.Failure
import com.basilalasadi.iti.kotlin.productsstateflow.data.repository.ProductsRepository.ProductsState.Loading
import com.basilalasadi.iti.kotlin.productsstateflow.data.repository.ProductsRepository.ProductsState.Success
import com.basilalasadi.iti.kotlin.productsstateflow.ui.theme.ViewModelSharingTheme
import com.basilalasadi.iti.kotlin.productsstateflow.ui.viewmodel.ProductsViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ViewModelSharingTheme {
                ProductsListScreen(
                    isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsListScreen(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
) {
    val viewModel = viewModel<ProductsViewModel>(factory = ProductsViewModel.Factory(LocalContext.current))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedProduct = rememberSaveable { mutableStateOf<FavoritableProduct?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val productsState = viewModel.products.collectAsStateWithLifecycle().value

    LaunchedEffect(scope) {
        scope.launch {
            viewModel.fetchRemoteProducts()
        }

        scope.launch {
            viewModel.message.collect {
                if (it.isNotBlank()) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    launch { snackbarHostState.showSnackbar(it) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, FavoritesActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorites")
                    }
                },
            )
        },
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Row(
            modifier = Modifier.padding(contentPadding),
        ) {
            ProductsListContainer(
                products = productsState.let {
                    when (it) {
                        is Success -> it.products
                        is Loading -> it.products
                        is Failure -> it.products
                    }
                },
                isLoading = productsState is Loading,
                errorMessage = (productsState as? Failure)?.error?.message,
                reloadProducts = { viewModel.fetchRemoteProducts() },
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

@Composable
fun ProductsListContainer(
    modifier: Modifier = Modifier,
    products: List<FavoritableProduct>?,
    errorMessage: String?,
    isLoading: Boolean,
    reloadProducts: (() -> Unit)?,
    onProductSelected: (FavoritableProduct) -> Unit,
    onProductSetFavorite: (FavoritableProduct) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        if (errorMessage != null) {
            ErrorCard(message = errorMessage, onClick = reloadProducts)
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(72.dp)
                    .padding(16.dp)
            )
        }
        if (products != null) {
            ProductsList(
                products = products,
                onProductSelected = onProductSelected,
                onProductSetFavorite = onProductSetFavorite,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsList(
    modifier: Modifier = Modifier,
    products: List<FavoritableProduct>,
    onProductSelected: (FavoritableProduct) -> Unit,
    onProductSetFavorite: (FavoritableProduct) -> Unit,
) {
     LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = modifier
        ) {
            itemsIndexed(products) { _, product ->
                ProductCard(
                    product = product,
                    onClick = { onProductSelected(product) },
                    onActionClick = { onProductSetFavorite(product) },
                )
            }
        }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: FavoritableProduct,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            ) {
                GlideImage(
                    model = product.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFFFFFFF), MaterialTheme.shapes.small)
                        .clip(MaterialTheme.shapes.small)
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = product.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = product.brand ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "\$%.2f".format(product.price),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            IconButton(
                onClick = { onActionClick() }
            ) {
                Icon(
                    imageVector =
                        if (product.isFavorite == true) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                    contentDescription =
                        if (product.isFavorite == true) {
                            "Add to Favorites"
                        } else {
                            "Remove from Favorites"
                        },
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun ErrorCard(modifier: Modifier = Modifier, message: String?, onClick: (() -> Unit)? = null) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Text(
                text = message ?: "An unknown error has occurred.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
            )
            IconButton(
                onClick = { onClick?.invoke() },
                modifier = Modifier
                    .padding(0.dp)
                    .then(Modifier.size(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                )
            }
        }
    }
}

//@Preview(showSystemUi = true)
@Composable
private fun ProductsListScreenPreview() {
    ProductsListScreen(
        isLandscape = false,
    )
}

@Preview
@Composable
private fun ErrorCardPreview() {
    ErrorCard(message = "Not connected to the internet.", onClick = {})
}

//@Preview
@Composable
private fun ProductCardPreview() {
    ProductCard(
        product = FavoritableProduct(1, "Product 1 with a Long Title That Overflows", "Product 1 description.", "Category", 8.99, "https://picsum.photos/256?i=1", "Brand", 4.45, true),
        onClick = {},
        onActionClick = {},
    )
}
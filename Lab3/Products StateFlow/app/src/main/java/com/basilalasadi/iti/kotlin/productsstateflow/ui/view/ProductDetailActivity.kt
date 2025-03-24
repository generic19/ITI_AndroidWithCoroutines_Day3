package com.basilalasadi.iti.kotlin.productsstateflow.ui.view

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.ui.theme.ViewModelSharingTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class ProductDetailActivity : ComponentActivity() {
    companion object {
        const val EXTRA_PRODUCT = "product"
    }

    private lateinit var product: FavoritableProduct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        product = intent.getSerializableExtra(EXTRA_PRODUCT) as FavoritableProduct

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish()
        }

        setContent {
            ViewModelSharingTheme {
                ProductDetailScreen(product)
            }
        }
    }
}

@Composable
fun ProductDetailScreen(product: FavoritableProduct) {
    ProductDetail(
        product = product,
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProductDetail(modifier: Modifier = Modifier, product: FavoritableProduct) {
    Surface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            GlideImage(
                model = product.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Text(
                text = product.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "\$%.2f".format(product.price),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                for (i in 1 .. 5) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (i < product.rating)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductDetailScreenPreview() {
    ProductDetailScreen(
        product = FavoritableProduct(1, "Product 1", "Product 1 description.", "Category", 8.99, "https://picsum.photos/256?i=1", "Brand", 4.45, false),
    )
}

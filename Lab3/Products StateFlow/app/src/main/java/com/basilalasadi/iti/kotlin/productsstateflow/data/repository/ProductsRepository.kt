package com.basilalasadi.iti.kotlin.productsstateflow.data.repository

import com.basilalasadi.iti.kotlin.productsstateflow.data.ProductsException
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.ProductsLocalDataSource
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.remote.ProductsRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class ProductsRepository(
    private val localDataSource: ProductsLocalDataSource,
    private val remoteDataSource: ProductsRemoteDataSource,
) {
    private val mutableProductsStateFlow = MutableStateFlow<ProductsState>(ProductsState.Loading(null))
    val productsStateFlow: StateFlow<ProductsState> = mutableProductsStateFlow

    private lateinit var scope: CoroutineScope

    private var collectionJob: Job? = null

    fun setScope(scope: CoroutineScope) {
        this.scope = scope
    }

    fun getFavoriteProductsFlow(): Flow<List<FavoritableProduct>> = localDataSource.favorites

    suspend fun setFavorite(productId: Long, isFavorite: Boolean): Boolean {
        return localDataSource.setFavorite(productId, isFavorite)
    }

    fun fetchRemoteProducts() = scope.launch(Dispatchers.IO) {
        collectionJob?.cancelAndJoin()

        collectionJob = launch {
            collectProducts {
                val products = remoteDataSource.getProducts()
                localDataSource.setProducts(products)
            }
        }
    }

    private suspend fun collectProducts(load: suspend () -> Unit) {
        var job: Job? = null

        try {
            job = scope.launch {
                localDataSource.products.cancellable().collectLatest {
                    mutableProductsStateFlow.value = ProductsState.Loading(it)
                }
            }

            load()
            job.cancelAndJoin()

            localDataSource.products.cancellable().collectLatest {
                mutableProductsStateFlow.value = ProductsState.Success(it)
            }
        } catch (ex: ProductsException) {
            job?.cancelAndJoin()

            localDataSource.products.cancellable().collectLatest {
                mutableProductsStateFlow.value = ProductsState.Failure(
                    products = it,
                    error = ex,
                )
            }
        } catch (_: CancellationException) {
            job?.cancelAndJoin()
        }
    }

    sealed class ProductsState {
        data class Loading(val products: List<FavoritableProduct>?) : ProductsState()
        data class Success(val products: List<FavoritableProduct>) : ProductsState()
        data class Failure(val products: List<FavoritableProduct>?, val error: ProductsException) : ProductsState()
    }
}

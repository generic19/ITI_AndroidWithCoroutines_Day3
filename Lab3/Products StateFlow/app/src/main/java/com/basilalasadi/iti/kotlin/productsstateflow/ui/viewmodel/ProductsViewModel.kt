package com.basilalasadi.iti.kotlin.productsstateflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.basilalasadi.iti.kotlin.productsstateflow.data.repository.ProductsRepository
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.ProductsLocalDataSourceImpl
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database.AppDatabase
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.remote.ProductsRemoteDataSourceImpl
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.remote.ProductsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class ProductsViewModel(private val repository: ProductsRepository) : ViewModel() {
    val products = repository.productsStateFlow
    val favoriteProducts = repository.getFavoriteProductsFlow()

    private val mutableMessage = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val message: SharedFlow<String> = mutableMessage

    fun fetchRemoteProducts() = repository.fetchRemoteProducts()

    fun setFavorite(productId: Long, isFavorite: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val changed = repository.setFavorite(productId, isFavorite)

        if (changed) {
            mutableMessage.emit(if (isFavorite) "Added to favorites." else "Removed from favorites.")
        } else {
            mutableMessage.emit(if (isFavorite) "Already in favorites." else "Could not add to favorites.")
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val productDao = AppDatabase.getInstance(context).getFavoritableProductDao()
            val productsService: ProductsService = ProductsService.create()

            val repository = ProductsRepository(
                localDataSource = ProductsLocalDataSourceImpl(productDao),
                remoteDataSource = ProductsRemoteDataSourceImpl(productsService),
            )

            val viewModel = ProductsViewModel(repository)
            repository.setScope(viewModel.viewModelScope)

            return viewModel as T
        }
    }
}
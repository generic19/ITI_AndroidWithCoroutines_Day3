package com.basilalasadi.iti.kotlin.productsstateflow.data.source.local

import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.Product
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database.FavoritableProductDao
import com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database.SetFavoriteProductDto
import kotlinx.coroutines.flow.Flow

interface ProductsLocalDataSource {
    val products: Flow<List<FavoritableProduct>>
    val favorites: Flow<List<FavoritableProduct>>

    suspend fun setProducts(products: List<Product>)
    suspend fun setFavoritableProducts(products: List<FavoritableProduct>)
    suspend fun setFavorite(productId: Long, isFavorite: Boolean): Boolean
}

class ProductsLocalDataSourceImpl(private val productDao: FavoritableProductDao) : ProductsLocalDataSource {
    override val products: Flow<List<FavoritableProduct>> by lazy {
        productDao.getAll()
    }

    override val favorites: Flow<List<FavoritableProduct>> by lazy {
        productDao.getFavorites()
    }

    override suspend fun setProducts(products: List<Product>) {
        productDao.setProducts(products)
    }

    override suspend fun setFavoritableProducts(products: List<FavoritableProduct>) {
        productDao.setFavoritableProducts(products)
    }

    override suspend fun setFavorite(productId: Long, isFavorite: Boolean): Boolean {
        return productDao.setFavorite(SetFavoriteProductDto(productId, isFavorite)) > 0
    }
}

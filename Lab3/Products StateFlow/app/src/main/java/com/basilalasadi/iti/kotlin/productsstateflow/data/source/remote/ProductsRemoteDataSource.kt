package com.basilalasadi.iti.kotlin.productsstateflow.data.source.remote

import com.basilalasadi.iti.kotlin.productsstateflow.data.ProductsException
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.Product
import java.io.IOException

interface ProductsRemoteDataSource {
    suspend fun getProducts(): List<Product>
}

class ProductsRemoteDataSourceImpl(private val productsService: ProductsService) : ProductsRemoteDataSource {
    override suspend fun getProducts(): List<Product> {
        try {
            val response = productsService.getProducts()

            if (response.isSuccessful) {
                val products = response.body()?.products

                if (products == null) {
                    throw ProductsException("API returned empty response.")
                }

                return products
            } else {
                throw ProductsException(response.message())
            }
        } catch (ex: IOException) {
            throw ProductsException("Could not reach the products API.", ex)
        }
    }
}

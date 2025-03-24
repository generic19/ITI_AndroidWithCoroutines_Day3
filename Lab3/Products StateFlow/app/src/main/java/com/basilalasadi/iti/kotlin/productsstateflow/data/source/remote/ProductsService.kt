package com.basilalasadi.iti.kotlin.productsstateflow.data.source.remote

import com.basilalasadi.iti.kotlin.productsstateflow.data.model.ProductsDto
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ProductsService {
    @GET("products")
    suspend fun getProducts(): Response<ProductsDto>

    companion object {
        fun create(): ProductsService = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductsService::class.java)
    }
}

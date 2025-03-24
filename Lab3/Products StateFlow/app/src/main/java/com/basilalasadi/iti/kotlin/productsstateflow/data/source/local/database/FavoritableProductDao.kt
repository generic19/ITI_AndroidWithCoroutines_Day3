package com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.FavoritableProduct
import com.basilalasadi.iti.kotlin.productsstateflow.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritableProductDao {
    @Query("select * from FavoritableProduct")
    fun getAll(): Flow<List<FavoritableProduct>>

    @Upsert(entity = FavoritableProduct::class)
    suspend fun addAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllFavoritables(products: List<FavoritableProduct>)

    @Query("delete from FavoritableProduct where isFavorite = 0")
    suspend fun removeAll()

    @Transaction
    suspend fun setProducts(products: List<Product>) {
        removeAll()
        addAll(products)
    }

    @Transaction
    suspend fun setFavoritableProducts(products: List<FavoritableProduct>) {
        removeAll()
        addAllFavoritables(products)
    }

    @Query("select * from FavoritableProduct where isFavorite = 1")
    fun getFavorites(): Flow<List<FavoritableProduct>>

    @Update(entity = FavoritableProduct::class)
    suspend fun setFavorite(dto: SetFavoriteProductDto): Int
}

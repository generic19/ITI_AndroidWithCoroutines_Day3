package com.basilalasadi.iti.kotlin.productsstateflow.data.source.local.database

import androidx.room.Entity

@Entity
data class SetFavoriteProductDto(
    val id: Long,
    val isFavorite: Boolean,
)
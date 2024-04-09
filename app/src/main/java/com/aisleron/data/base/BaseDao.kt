package com.aisleron.data.base

import androidx.room.Delete
import androidx.room.Upsert

interface BaseDao<T> {
    @Upsert
    suspend fun upsert(vararg entity: T): List<Long>

    @Delete
    suspend fun delete(vararg entity: T)
}
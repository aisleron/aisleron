package com.aisleron.data.base

import androidx.room.Delete
import androidx.room.Upsert

interface BaseDao<T> {
    @Upsert
    fun upsert(vararg entity: T)

    @Delete
    fun delete(vararg entity: T)
}
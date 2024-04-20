package com.aisleron.domain.base

interface BaseRepository<T> {
    suspend fun get(id: Int): T?
    suspend fun getMultiple(vararg id: Int): List<T>
    suspend fun getAll(): List<T>
    suspend fun add(item: T): Int
    suspend fun update(item: T)
    suspend fun update(items: List<T>)
    suspend fun remove(item: T)
}
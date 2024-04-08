package com.aisleron.domain.base

interface BaseRepository<T> {
    suspend fun get(id: Int): T?
    suspend fun getAll(): List<T>
    suspend fun add(item: T)
    suspend fun update(item: T)
    suspend fun remove(item: T)
    suspend fun remove(id: Int)
}
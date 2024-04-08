package com.aisleron.domain.base

interface BaseRepository<T> {
    suspend fun get(id: Int): T?
    fun getAll(): List<T>
    fun add(item: T)
    fun update(item: T)
    fun remove(item: T)
    fun remove(id: Int)
}
package com.aisleron.domain.repository

interface BaseRepository<T> {
    fun get(id: Long): T
    fun getAll(): List<T>
    fun add(item: T)
    fun update(item: T)
    fun remove(item: T)
    fun remove(id: Long)
}
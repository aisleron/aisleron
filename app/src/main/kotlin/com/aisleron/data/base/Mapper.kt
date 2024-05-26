package com.aisleron.data.base

interface Mapper<Entity : Any, Model : Any> {
    fun toModel(value: Entity): Model
    fun fromModel(value: Model): Entity
    fun toModelList(list: List<Entity>): List<Model>
    fun fromModelList(list: List<Model>): List<Entity>
}
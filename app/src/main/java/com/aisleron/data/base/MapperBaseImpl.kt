package com.aisleron.data.base

abstract class MapperBaseImpl<Entity : Any, Model : Any> : Mapper<Entity, Model> {
    abstract override fun toModel(value: Entity): Model

    abstract override fun fromModel(value: Model): Entity

    override fun toModelList(list: List<Entity>): List<Model> {
        return list.map { toModel(it) }
    }

    override fun fromModelList(list: List<Model>): List<Entity> {
        return list.map { fromModel(it) }
    }
}
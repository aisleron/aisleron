/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.base

interface Mapper<Entity : Any, Model : Any> {
    fun toModel(value: Entity): Model

    // TODO: replace with fun fromModel(value: Model, toEntity: Entity?): Entity
    fun fromModel(value: Model): Entity

    fun toModelList(list: List<Entity>): List<Model> {
        return list.map { toModel(it) }
    }

    // TODO: Delete this method, it doesn't allow for mapping existing entities
    fun fromModelList(list: List<Model>): List<Entity> {
        return list.map { fromModel(it) }
    }
}
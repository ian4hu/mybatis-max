/*
 *    Copyright 2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
@file:Suppress("ktlint:standard:function-naming")

package com.github.ian4hu.mybatis.exposed.dsl

import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.Renderable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * @author ian
 * @date 2026/01/14
 */
class SelectDSL(val selectClause: MutableList<Renderable>) {
    operator fun Renderable.unaryPlus(): Tracker {
        selectClause.add(this)
        return Tracker(selectClause, this)
    }

    operator fun Renderable.unaryMinus() {
        selectClause.remove(this)
    }

    operator fun String.unaryPlus(): Tracker {
        val value = Expr.column(this)
        selectClause.add(value)
        return Tracker(selectClause, value)
    }

    operator fun String.unaryMinus() {
        val value = Expr.column(this)
        selectClause.remove(value)
    }

    inline operator fun <reified T> KProperty1<T, *>.unaryPlus(): Tracker {
        val value = Expr.kotlinProperty(this)
        selectClause.add(value)
        return Tracker(selectClause, value)
    }

    inline operator fun <reified T> KProperty1<T, *>.unaryMinus() {
        val value = Expr.kotlinProperty(this)
        selectClause.remove(value)
    }

    @Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")
    operator fun <T : Any> KClass<T>.unaryPlus() {
        members
            .filterIsInstance<KProperty1<*, *>>()
            .map { it as KProperty1<T, *> }
            .map { Expr.kotlinProperty(it, this.java) }
            .apply { selectClause.addAll(this) }
    }
}

/**
 * A tracker to carry a [Renderable] and make it aliasable
 */
class Tracker(private val selectClause: MutableList<Renderable>, private val value: Renderable) {

    infix fun AS(alias: String) {
        selectClause.find { it === value } ?: throw IllegalArgumentException("Column can't be aliased, may the original column $value is already aliased or been removed.")
        // Replace the original column with an aliased one
        selectClause.replaceAll { if (it === value) it.alias(alias) else it }
    }
}

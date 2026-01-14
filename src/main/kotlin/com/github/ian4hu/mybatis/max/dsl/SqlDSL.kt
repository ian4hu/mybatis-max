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
package com.github.ian4hu.mybatis.max.dsl

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.query.Query
import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Renderable
import com.github.ian4hu.mybatis.max.render.addCondition
import com.github.ian4hu.mybatis.max.render.addSelect

/**
 * @author ian
 * @date 2026/01/14
 */
class SqlDSL {

    val selectClause = arrayListOf<Renderable>()
    val whereCondition = arrayListOf<Condition>()

    fun <T> renderTo(wrapper: T): T where T : AbstractWrapper<*, *, T>, T : Query<T, *, *> {
        wrapper.addSelect(*selectClause.toTypedArray())
        whereCondition.firstOrNull() ?.let { wrapper.addCondition(it) }
        return wrapper
    }

    fun select(init: SelectDSL.() -> Unit) = SelectDSL(selectClause).apply(init)

    fun where(init: WhereDSL.() -> Condition) {
        if (whereCondition.isNotEmpty()) {
            throw IllegalArgumentException("WHERE clause is specified twice.")
        }
        whereCondition.add(WhereDSL().init())
    }
}

fun sql(init: SqlDSL.() -> Unit) = SqlDSL().apply(init)
fun <T> sql(wrapper: T, init: SqlDSL.() -> Unit): T where T : AbstractWrapper<*, *, T>, T : Query<T, *, *> = sql(init).renderTo(wrapper)

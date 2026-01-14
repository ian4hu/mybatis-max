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
package com.github.ian4hu.mybatis.exposed.render

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.baomidou.mybatisplus.core.conditions.query.Query
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.exposed.Condition
import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.Render
import com.github.ian4hu.mybatis.exposed.Renderable
import com.github.ian4hu.mybatis.exposed.expr.CompositeExpr
import kotlin.reflect.KProperty1

/**
 * MyBatis-Plus wrapper-based implementation of [Render].
 *
 * Delegates rendering operations to MyBatis-Plus's [Helper] utility methods,
 * providing integration with MyBatis-Plus's dynamic SQL generation.
 *
 * @property wrapper the MyBatis-Plus wrapper context for SQL generation
 */
class WrapperRender(val wrapper: AbstractWrapper<*, *, *>) : Render {
    override fun column(column: String): String = Helper.wrapColumn(wrapper, column)

    override fun lambda(lambda: SFunction<*, *>): String = Helper.wrapLambda(wrapper, lambda)

    override fun <T> kotlinProperty(property: KProperty1<T, *>, entityClass: Class<T>): String = Helper.wrapProperty(wrapper, property, entityClass)

    override fun formatParam(param: Any?, mapping: String?): String = Helper.wrapParam(wrapper, param, mapping)
}

/**
 * Conditionally adds select fields based on boolean flags.
 *
 * Only fields with `true` condition are included in the SELECT clause.
 *
 * @param fields pairs of (field, includeCondition)
 * @return this wrapper for chaining
 */
fun <T> T.addSelect(vararg fields: Pair<Renderable, Boolean>): T where T : AbstractWrapper<*, *, T>, T : Query<*, *, *> = this.addSelect(*fields.filter { it.second }.map { it.first }.toTypedArray())

/**
 * Clears the SELECT clause from the query.
 *
 * @param condition if true, clears the SELECT clause; otherwise, no-op
 * @return this wrapper for chaining
 */
fun <T> T.clearSelect(condition: Boolean = true): T where T : AbstractWrapper<*, *, T>, T : Query<*, *, *> {
    if (condition) {
        Helper.getSqlSelect(this).stringValue = null
    }
    return this
}

/**
 * Adds custom select fields using [Renderable] expressions.
 *
 * Renders each field and appends to the existing SELECT clause.
 *
 * @param fields the renderable fields to add
 * @return this wrapper for chaining
 */
fun <T> T.addSelect(vararg fields: Renderable): T where T : AbstractWrapper<*, *, T>, T : Query<*, *, *> {
    if (fields.isEmpty()) return this
    val render = WrapperRender(this)
    val selectClause = Helper.getSqlSelect(this)
    val rendered = fields
        .map { it.render(render) }
    val snippet = listOf(selectClause.stringValue).plus(rendered)
        .filter { !it.isNullOrBlank() }
        .joinToString(StringPool.COMMA)
    selectClause.stringValue = snippet
    return this
}

/**
 * Adds a condition to the WHERE clause using AND logic.
 *
 * The condition is wrapped in a nested segment to preserve precedence.
 *
 * @param condition the condition to add
 * @return this wrapper for chaining
 */
fun <T : AbstractWrapper<*, *, T>> T.addCondition(condition: Condition): T {
    if (condition !is CompositeExpr) {
        return apply(condition.render(WrapperRender(this)))
    }
    return nested { it.apply(condition.render(WrapperRender(it))) }
}

/**
 * Alias for [addCondition] - adds a condition using AND logic.
 */
fun <T : AbstractWrapper<*, *, T>> T.and(condition: Condition): T = this.addCondition(condition)

/**
 * Adds a condition to the WHERE clause using OR logic.
 *
 * @param condition the condition to add
 * @return this wrapper for chaining
 */
fun <T : AbstractWrapper<*, *, T>> T.or(condition: Condition): T = this.or().addCondition(condition)

/**
 * Clears all conditions from the WHERE clause.
 *
 * @param condition if true, clears conditions; otherwise, no-op
 * @return this wrapper for chaining
 */
fun <T : AbstractWrapper<*, *, T>> T.clearCondition(condition: Boolean = true): T {
    if (condition) {
        // Trigger an update for [MergeSegments]'s cacheSqlSegment
        addCondition(Condition.and(Expr.literal("TRUE").asCondition(), Expr.literal("TRUE").asCondition()))
        expression.normal.clear()
    }
    return this
}

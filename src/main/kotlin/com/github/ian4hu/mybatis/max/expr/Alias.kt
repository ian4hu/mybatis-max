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
package com.github.ian4hu.mybatis.max.expr

import com.github.ian4hu.mybatis.max.Render
import com.github.ian4hu.mybatis.max.Renderable

/**
 * Represents a SQL alias wrapper that renders an expression with an optional alias name.
 *
 * An alias is used in SQL to provide alternative names for expressions, typically in SELECT clauses
 * (e.g., `COUNT(*) AS total` or `user_name AS name`). This class wraps any [Renderable] expression
 * and appends the alias during SQL generation.
 *
 * If the wrapped expression is a [CompositeExpr], it will be automatically enclosed in parentheses
 * to ensure correct SQL syntax.
 *
 * @param T the type of the aliased expression's value
 * @property alias the SQL alias name (can be blank to render without alias)
 * @property expr the underlying renderable expression to be aliased
 */
data class Alias(
    val alias: String,
    val expr: Renderable,
) : Renderable {

    init {
        if (alias.isNotBlank() && !isValidIdentifier(alias)) throw IllegalArgumentException("'$alias' is not valid alias")
    }

    /**
     * Renders the aliased expression into a SQL fragment.
     *
     * Composite expressions are wrapped in parentheses. If the alias is blank,
     * only the expression is rendered. Otherwise, renders as "expression AS alias".
     *
     * @param render the MyBatis-Plus wrapper context used for SQL generation
     * @return the rendered SQL fragment with optional alias
     */
    override fun render(render: Render): String {
        val rendered = if (expr is CompositeExpr) "(${expr.render(render)})" else expr.render(render)
        return if (alias.isBlank()) rendered else "$rendered AS $alias"
    }

    companion object {
        fun isValidIdentifier(value: String): Boolean = value.matches(LiteralExpr.IDENTIFIER_REGEX)
    }
}

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
package com.github.ian4hu.mybatis.exposed.expr

import com.github.ian4hu.mybatis.exposed.Render
import com.github.ian4hu.mybatis.exposed.Renderable

/**
 * SQL alias wrapper that renders an expression with an optional alias name.
 *
 * Aliases provide alternative names for expressions in SELECT clauses
 * (e.g., `COUNT(*) AS total` or `user_name AS name`).
 *
 * Composite expressions are automatically wrapped in parentheses.
 *
 * @property alias the SQL alias name (validated as identifier, can be blank)
 * @property expr the underlying renderable expression
 */
data class Alias(
    val alias: String,
    val expr: Renderable,
) : Renderable {

    init {
        if (alias.isNotBlank() && !isValidIdentifier(alias)) throw IllegalArgumentException("'$alias' is not valid alias")
    }

    override fun render(render: Render): String {
        val rendered = if (expr is CompositeExpr) "(${expr.render(render)})" else expr.render(render)
        return if (alias.isBlank()) rendered else "$rendered AS $alias"
    }

    companion object {
        /** Checks if the value is a valid SQL identifier. */
        fun isValidIdentifier(value: String): Boolean = value.matches(LiteralExpr.IDENTIFIER_PATTERN)
    }
}

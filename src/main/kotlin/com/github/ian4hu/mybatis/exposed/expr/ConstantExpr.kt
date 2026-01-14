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

import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.Render

/**
 * Constant value expression with type-based rendering.
 *
 * Rendering strategy:
 * - **Null**: Rendered as `NULL` keyword
 * - **Boxed primitives** (Boolean, Byte, Short, Int, Long, Float, Double): Rendered as SQL literals (e.g., `42`, `true`, `3.14`)
 * - **Safe strings**: If string matches literal patterns, rendered as literal
 * - **Other types**: Treated as parameterized variables for security
 *
 * @property value the constant value (can be null)
 */
data class ConstantExpr(
    val value: Any?,
) : Expr {
    override fun render(render: Render): String {
        if (value == null) {
            return Expr.literal("NULL").render(render)
        }

        // Boxed primitives render as literals
        if (isBoxedPrimitive(value)) {
            return Expr.literal(value.toString()).render(render)
        }

        // Safe strings render as literals
        if (value is String && LiteralExpr.isSafeLiteral(value)) {
            return Expr.literal(value).render(render)
        }

        // Everything else becomes a variable
        return Expr.variable(value).render(render)
    }

    /** Checks if the value is a boxed primitive type (Boolean, Byte, Short, Int, Long, Float, Double). */
    private fun isBoxedPrimitive(value: Any): Boolean = when (value) {
        is Boolean -> true
        is Byte -> true
        is Short -> true
        is Int -> true
        is Long -> true
        is Float -> true
        is Double -> true
        else -> false
    }
}

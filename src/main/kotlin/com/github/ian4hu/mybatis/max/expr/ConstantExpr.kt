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

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

/**
 * Represents a constant value expression with intelligent rendering based on value type.
 *
 * Constants are rendered differently depending on their type:
 * - **Null values**: Rendered as the SQL keyword `NULL`
 * - **Primitive types** (boolean, byte, short, int, long, float, double) and their boxed equivalents:
 *   Rendered directly as SQL literals (e.g., `42`, `true`, `3.14`)
 * - **Non-primitive types** (strings, objects, collections, etc.): Treated as parameterized variables
 *   for security, preventing SQL injection
 *
 * This automatic type-based handling provides a convenient way to include values in SQL expressions
 * while maintaining security for complex types.
 *
 * @property value the constant value (can be null)
 */
data class ConstantExpr(
    val value: Any?,
) : Expr {
    /**
     * Renders the constant value into appropriate SQL representation.
     *
     * The rendering strategy depends on the value type:
     * - Null → `NULL` keyword
     * - Primitives/boxed primitives → Direct literal representation
     * - Other types → Parameterized variable (safe from SQL injection)
     *
     * @param wrapper the MyBatis-Plus wrapper context used for rendering
     * @return the SQL representation of this constant
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        if (value == null) {
            return Expr.literal("NULL").render(wrapper)
        }

        // Direct render primitive value
        if (isBoxedPrimitive(value)) {
            return Expr.literal(value.toString()).render(wrapper)
        }

        if (value is String && LiteralExpr.isSafeLiteral(value)) {
            return Expr.literal(value).render(wrapper)
        }

        // Non primitive value will take as variable
        return Expr.variable(value).render(wrapper)
    }

    /**
     * Checks if the value is a boxed primitive wrapper type.
     *
     * Boxed primitives include: Boolean, Byte, Short, Int, Long, Float, Double.
     *
     * @param value the value to check
     * @return true if the value is a boxed primitive type
     */
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

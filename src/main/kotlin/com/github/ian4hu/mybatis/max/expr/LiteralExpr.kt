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
 * Literal SQL expression that renders directly without escaping.
 *
 * Literals are used for SQL identifiers (column names, table names) or keywords.
 * Only values matching safe patterns are accepted to prevent SQL injection.
 *
 * Use [Expr.literal] factory method which validates input before creating instances.
 *
 * @property value the raw SQL literal string (validated on construction)
 */
data class LiteralExpr(
    val value: String,
) : Expr {

    init {
        if (!isSafeLiteral(value)) throw IllegalArgumentException("'$value' is not a valid literal. Only SQL identifiers, numbers, and booleans are allowed.")
    }

    /**
     * Renders the literal value directly into SQL.
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = value

    companion object {
        /**
         * Pattern for validating safe SQL identifier literals.
         *
         * Matches: alphanumeric strings starting with a letter or underscore.
         *
         * Valid: `user_name`, `COUNT`, `table1`, `_temp`, `NULL`
         *
         * Invalid: `user-name`, `1user`, `user name`, `user;DROP`
         */
        val IDENTIFIER_REGEX = Regex("(?i)^[_A-Z][_A-Z0-9]*$")

        /**
         * Pattern for validating safe SQL numeric literals.
         *
         * Matches: integers, decimals, scientific notation.
         *
         * Valid: `42`, `-123`, `3.14`, `-0.5`, `1.23E10`
         */
        val NUMERIC_REGEX = Regex("^[+-]?\\d*\\.?\\d+([eE][+-]?\\d+)?$")

        /**
         * Checks if the value is a safe SQL literal.
         *
         * Safe literals: SQL identifiers or numeric values.
         */
        fun isSafeLiteral(value: String): Boolean = value.matches(IDENTIFIER_REGEX) ||
            value.matches(NUMERIC_REGEX)
    }
}

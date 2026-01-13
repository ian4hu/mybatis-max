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

import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render

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

    override fun render(render: Render): String = value

    companion object {

        /**
         * Maps quote characters to their corresponding content validation patterns.
         *
         * Each pattern validates characters allowed inside quoted strings, excluding
         * the quote character itself (handled separately via escape/double-quote logic).
         */
        val QUOTE_CHARS_AND_LITERAL_PATTERN = mapOf(
            '"' to Regex("^[a-zA-Z0-9+-. :;+\\-*/<>,~!@#%^&()?$\\[\\]`']*$"),
            '\'' to Regex("^[a-zA-Z0-9+-. :;+\\-*/<>,~!@#%^&()?$\\[\\]`\"]*$"),
            '`' to Regex("^[a-zA-Z0-9+-. :;+\\-*/<>,~!@#%^&()?$\\[\\]'\"]*$"),
        )

        /**
         * Pattern for validating simple SQL identifier literals.
         *
         * Matches: alphanumeric strings starting with a letter or underscore.
         *
         * Valid: `user_name`, `COUNT`, `table1`, `_temp`, `NULL`
         *
         * Invalid: `user-name`, `1user`, `user name`, `user;DROP`, `user.col`
         */
        val IDENTIFIER_PATTERN = Regex("^[_A-Za-z][_A-Za-z0-9]*$")

        /**
         * Pattern for validating SQL column references including qualified names.
         *
         * Matches: identifiers with optional dot-separated qualifiers (table.column).
         *
         * Valid: `user_name`, `COUNT`, `table1`, `t.col`, `schema.table.col`, `NULL`
         *
         * Invalid: `user-name`, `1user`, `user name`, `user;DROP`, `.col`, `table.`
         */
        val COLUMN_REGEX = Regex("^[_A-Za-z][_A-Za-z0-9]*(\\.?[_A-Za-z0-9]+)*$")

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
         * Safe literals include:
         * - SQL identifiers (qualified columns, table names)
         * - Numeric values
         * - Simple quoted strings (with proper escaping)
         */
        fun isSafeLiteral(value: String): Boolean = value.matches(COLUMN_REGEX) ||
            value.matches(NUMERIC_REGEX) || isSimpleQuotedLiteral(value)

        /**
         * Validates simple quoted string literals.
         *
         * Accepts strings enclosed in matching quotes (', ", `) with safe content.
         * Supports escaped quotes (`\'`) and doubled quotes (`''`).
         *
         * Valid: `''` (empty), `'123'`, `'$.json.path[0]'`, `'hello world'`, `"name"`
         *
         * Invalid: `'unclosed`, `'a'b'` (unescaped quote), `'DROP TABLE;'` (semicolon)
         */
        fun isSimpleQuotedLiteral(value: String): Boolean {
            if (value.isEmpty() || value.length < 2) return false
            val firstChar = value[0]
            val lastChar = value.last()
            if (firstChar != lastChar) {
                return false
            }
            // Quote char allow list check
            val literalPattern = QUOTE_CHARS_AND_LITERAL_PATTERN[firstChar] ?: return false

            // Allow double quote char
            val withoutDoubleQuote = value.drop(1).dropLast(1)
                .replace("\\$firstChar", "") // strip escaped quote
                .replace("$firstChar$firstChar", "") // strip double quote
            return withoutDoubleQuote.matches(literalPattern)
        }

        /**
         * Validates SQL function names using column reference pattern.
         */
        fun isValidFunctionName(name: String): Boolean = name.matches(COLUMN_REGEX)
    }
}

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
 * Represents a literal SQL expression that renders directly into SQL without escaping or parameterization.
 *
 * Literals are used for SQL identifiers (column names, table names) or keywords that must appear
 * as-is in the generated SQL. To prevent SQL injection vulnerabilities, only values matching
 * the pattern defined by [LITERAL_REGEX] are accepted (alphanumeric identifiers starting with
 * a letter or underscore).
 *
 * **Security Note**: Literals bypass SQL parameterization. Use [Expr.literal] factory method
 * which validates the input against [LITERAL_REGEX] before creating instances.
 *
 * @property value the raw SQL literal string (must match [LITERAL_REGEX] pattern)
 */
data class LiteralExpr(
    val value: String,
) : Expr<String> {
    /**
     * Renders the literal value directly into SQL without any modification.
     *
     * @param wrapper the MyBatis-Plus wrapper context (not used for literals)
     * @return the literal value as-is
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = value

    companion object {
        /**
         * Regular expression pattern for validating safe SQL literals.
         *
         * Matches valid SQL identifiers: alphanumeric strings (case-insensitive) that start with
         * a letter (A-Z) or underscore (_), followed by any combination of letters, digits (0-9),
         * or underscores.
         *
         * Examples of valid literals: `user_name`, `COUNT`, `table1`, `_temp`
         *
         * Examples of invalid literals: `user-name` (contains hyphen), `1user` (starts with digit),
         * `user name` (contains space), `user;DROP` (contains semicolon)
         */
        val LITERAL_REGEX = Regex("(?i)^[_A-Z][_A-Z0-9]*$")
    }
}

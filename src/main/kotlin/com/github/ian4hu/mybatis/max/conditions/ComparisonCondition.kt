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
package com.github.ian4hu.mybatis.max.conditions

import com.github.ian4hu.mybatis.max.ComparisonOp
import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render

/**
 * Binary comparison condition for SQL WHERE clauses.
 *
 * Represents a comparison between two expressions using standard SQL comparison operators.
 * The condition renders as: `left operator right`
 *
 * Examples:
 * - `age >= 18` - age greater than or equal to 18
 * - `name = 'John'` - name equals 'John'
 * - `price <> 0` - price not equal to 0
 * - `score <=> NULL` - null-safe equality check
 *
 * @property op the comparison operator (=, <>, >, >=, <, <=, <=>)
 * @property left the left operand expression
 * @property right the right operand expression
 */
data class ComparisonCondition(val op: ComparisonOp, val left: Expr, val right: Expr) : Condition {
    override fun render(render: Render): String = arrayOf(left, right)
        .joinToString(op.op) { it.render(render) }
}

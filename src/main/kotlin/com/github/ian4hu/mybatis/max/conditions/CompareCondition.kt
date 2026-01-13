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

import com.github.ian4hu.mybatis.max.CompareOp
import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render

/**
 * Binary comparison condition.
 *
 * Renders as: `left operator right` (e.g., `age >= 18`).
 *
 * @property op the comparison operator
 * @property left the left operand
 * @property right the right operand
 */
data class CompareCondition(val op: CompareOp, val left: Expr, val right: Expr) : Condition {
    override fun render(render: Render): String = arrayOf(left, right)
        .joinToString(op.operator) { it.render(render) }
}

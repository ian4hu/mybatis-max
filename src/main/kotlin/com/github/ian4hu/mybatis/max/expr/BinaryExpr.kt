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

import com.github.ian4hu.mybatis.max.BinaryOp
import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render
import com.github.ian4hu.mybatis.max.conditions.DummyCondition

/**
 * Binary expression combining multiple operands with a single operator.
 *
 * Automatically flattens nested expressions with the same operator and removes duplicates.
 * Composite sub-expressions are wrapped in parentheses to preserve precedence.
 *
 * @property op the binary operator (AND, OR, XOR, etc.)
 * @property elements the operand expressions
 */
data class BinaryExpr(val op: BinaryOp, val elements: List<Expr>) :
    Condition,
    CompositeExpr {
    override fun render(render: Render): String = elements.joinToString(" ${op.op} ") {
        if (it is CompositeExpr) {
            "(${it.render(render)})"
        } else {
            it.render(render)
        }
    }

    companion object {
        /**
         * Creates a binary expression with automatic optimization.
         *
         * Flattens nested expressions using the same operator and removes duplicates.
         * If only one element remains after optimization, returns it directly.
         *
         * @param op the binary operator
         * @param a the first operand
         * @param b the second operand
         * @param expr additional operands
         * @return optimized condition expression
         */
        fun of(op: BinaryOp, a: Expr, b: Expr, vararg expr: Expr): Condition {
            val elements = arrayOf(a, b, *expr)
                .flatMap { if (it is BinaryExpr && it.op == op) it.elements else listOf(it) }.distinct()
            if (elements.size == 1) {
                return DummyCondition.of(elements[0])
            }
            return BinaryExpr(op, elements)
        }
    }
}

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

import com.github.ian4hu.mybatis.exposed.Condition
import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.Render
import com.github.ian4hu.mybatis.exposed.UnaryBooleanOp
import com.github.ian4hu.mybatis.exposed.conditions.DummyCondition

/**
 * Unary boolean expression applying a single operator to an operand.
 *
 * Supports logical NOT and SQL-specific predicates (IS NULL, IS TRUE, etc.).
 * Composite operands are wrapped in parentheses to preserve precedence.
 * Double negation is automatically eliminated via [inverseOp].
 *
 * @property op the unary operator
 * @property expr the operand expression
 */
@ConsistentCopyVisibility
data class UnaryBooleanExpr private constructor(
    val op: UnaryBooleanOp,
    val expr: Expr,
) : Condition {
    override fun render(render: Render): String {
        val renderedExpr = if (expr is CompositeExpr) {
            "(${expr.render(render)})"
        } else {
            expr.render(render)
        }
        return if (op.prefix) "${op.op} $renderedExpr" else "$renderedExpr ${op.op}"
    }

    override fun not(): Condition = op.inverseOp.let { UnaryBooleanOp.valueOf(it) }.of(expr)

    companion object {
        /**
         * Creates a unary boolean expression.
         *
         * If operator is DUMMY (no-op), returns the expression directly as a condition.
         *
         * @param op the unary operator
         * @param expr the operand expression
         * @return condition with the operator applied
         */
        fun of(op: UnaryBooleanOp, expr: Expr): Condition {
            if (op == UnaryBooleanOp.DUMMY) return DummyCondition.of(expr)
            return UnaryBooleanExpr(op, expr)
        }
    }
}

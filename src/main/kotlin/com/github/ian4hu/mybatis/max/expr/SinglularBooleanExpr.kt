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

import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render
import com.github.ian4hu.mybatis.max.SingularBooleanOp
import com.github.ian4hu.mybatis.max.conditions.DummyCondition

/**
 * Logical NOT expression that negates another expression.
 *
 * Composite expressions are wrapped in parentheses.
 * Double negation is automatically eliminated.
 *
 * @property expr the expression to negate
 */
@ConsistentCopyVisibility
data class SinglularBooleanExpr private constructor(
    val op: SingularBooleanOp,
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

    override fun not(): Condition = op.inverseOp.let { SingularBooleanOp.valueOf(it) }.of(expr)

    companion object {
        fun of(op: SingularBooleanOp, expr: Expr): Condition {
            if (op == SingularBooleanOp.DUMMY) return DummyCondition.of(expr)
            return SinglularBooleanExpr(op, expr)
        }
    }
}

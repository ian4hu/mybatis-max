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

import com.github.ian4hu.mybatis.max.BiOp
import com.github.ian4hu.mybatis.max.Condition
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render
import com.github.ian4hu.mybatis.max.conditions.DummyCondition

/**
 * @author ian
 * @date 2026/01/13
 */
data class BiExpr(val op: BiOp, val elements: List<Expr>) :
    Condition,
    CompositeExpr {
    override fun render(render: Render): String = elements.joinToString(" ${op.name} ") {
        if (it is CompositeExpr) {
            "(${it.render(render)})"
        } else {
            it.render(render)
        }
    }

    companion object {
        /**
         * Creates an OR expression, flattening nested ORs and removing duplicates.
         * Returns a single expression if only one element remains after optimization.
         */
        fun of(op: BiOp, a: Expr, b: Expr, vararg expr: Expr): Condition {
            val elements = arrayOf(a, b, *expr)
                .flatMap { if (it is BiExpr && it.op == op) it.elements else listOf(it) }.distinct()
            if (elements.size == 1) {
                return DummyCondition.of(elements[0])
            }
            return BiExpr(op, elements)
        }
    }
}

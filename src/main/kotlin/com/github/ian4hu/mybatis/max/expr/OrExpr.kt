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
 * Logical OR expression that combines multiple expressions.
 *
 * Automatically flattens nested OR expressions and removes duplicates.
 * Non-OR composite expressions are wrapped in parentheses.
 *
 * @property elements the list of expressions to combine
 */
@ConsistentCopyVisibility
data class OrExpr private constructor(
    val elements: List<Expr>,
) : CompositeExpr {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = elements.joinToString(" OR ") {
        if (it is CompositeExpr && it !is OrExpr) {
            "(${it.render(wrapper)})"
        } else {
            it.render(wrapper)
        }
    }

    companion object {
        /**
         * Creates an OR expression, flattening nested ORs and removing duplicates.
         * Returns a single expression if only one element remains after optimization.
         */
        fun of(a: Expr, b: Expr, vararg expr: Expr): Expr {
            val elements = arrayOf(a, b, *expr)
                .flatMap { if (it is OrExpr) it.elements else listOf(it) }.distinct()
            if (elements.size == 1) {
                return elements[0]
            }
            return OrExpr(elements)
        }
    }
}

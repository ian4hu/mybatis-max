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
package com.github.ian4hu.mybatis.exposed

import com.github.ian4hu.mybatis.exposed.expr.BinaryExpr

/**
 * Binary operators for combining expressions.
 *
 * Supported operators:
 * - Logical: AND, OR, XOR
 * - Bitwise: &, |, ^
 *
 * @property op the SQL operator keyword or symbol
 */
enum class BinaryOp(val op: String) {
    AND("AND"),
    OR("OR"),
    XOR("XOR") {
        override fun optimize(elements: List<Expr>): List<Expr> {
            val flattened = super.flatten(this, elements)
            val parityMap = mutableMapOf<Expr, Boolean>()
            for (expr in flattened) {
                parityMap[expr] = !(parityMap[expr] ?: false)
            }
            return parityMap.filter { (_, keep) -> keep }.keys.toList()
        }
    },
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    ;

    /** Creates a binary expression with this operator, flattening nested same-operator expressions. */
    fun of(a: Expr, b: Expr, vararg expr: Expr): Condition = BinaryExpr.of(this, a, b, *expr)

    open fun optimize(elements: List<Expr>): List<Expr> = flatten(this, elements).distinct()

    internal fun flatten(op: BinaryOp, expr: Expr): List<Expr> = when {
        expr !is BinaryExpr -> listOf(expr)
        expr.op != op -> listOf(expr)
        else -> expr.elements.flatMap { flatten(op, it) }
    }

    internal fun flatten(op: BinaryOp, expr: List<Expr>): List<Expr> = expr.flatMap { flatten(op, it) }
}

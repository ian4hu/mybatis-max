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
package com.github.ian4hu.mybatis.max

import com.github.ian4hu.mybatis.max.expr.BiExpr

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
    XOR("XOR"),
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    ;

    /** Creates a binary expression with this operator, flattening nested same-operator expressions. */
    fun of(a: Expr, b: Expr, vararg expr: Expr): Condition = BiExpr.of(this, a, b, *expr)
}

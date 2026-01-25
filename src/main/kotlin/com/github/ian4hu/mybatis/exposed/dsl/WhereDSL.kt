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
package com.github.ian4hu.mybatis.exposed.dsl

import com.github.ian4hu.mybatis.exposed.ComparisonOp
import com.github.ian4hu.mybatis.exposed.Condition
import com.github.ian4hu.mybatis.exposed.Expr
import kotlin.reflect.KProperty1

/**
 * A scope for where condition
 */
class WhereDSL {

    operator fun String.unaryPlus(): Expr = Expr.column(this)

    inline operator fun <reified T> KProperty1<T, *>.unaryPlus(): Expr = Expr.kotlinProperty(this, T::class.java)

    // ============ Infix Comparison Operators ============

    /**
     * Infix equality operator: `expr eq value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this = other`
     */
    infix fun Expr.eq(other: Any?): Condition = this.equalTo(toExpr(other))

    /**
     * Infix inequality operator: `expr ne value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this <> other`
     */
    infix fun Expr.ne(other: Any?): Condition = ComparisonOp.NotEqualTo.of(this, toExpr(other))

    /**
     * Infix greater-than operator: `expr gt value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this > other`
     */
    infix fun Expr.gt(other: Any?): Condition = ComparisonOp.GreaterThan.of(this, toExpr(other))

    /**
     * Infix greater-or-equal operator: `expr ge value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this >= other`
     */
    infix fun Expr.ge(other: Any?): Condition = ComparisonOp.GreaterOrEqualTo.of(this, toExpr(other))

    /**
     * Infix less-than operator: `expr lt value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this < other`
     */
    infix fun Expr.lt(other: Any?): Condition = ComparisonOp.LessThan.of(this, toExpr(other))

    /**
     * Infix less-or-equal operator: `expr le value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this <= other`
     */
    infix fun Expr.le(other: Any?): Condition = ComparisonOp.LessOrEqualTo.of(this, toExpr(other))

    /**
     * Infix null-safe equality operator: `expr eqNullSafe value`
     *
     * @param other the value to compare with (automatically converted to Expr)
     * @return Condition representing `this <=> other`
     */
    infix fun Expr.eqNullSafe(other: Any?): Condition = ComparisonOp.NullSafeEqualTo.of(this, toExpr(other))

    // ============ Helper Function ============

    /**
     * Converts various types to Expr.
     * - Expr -> returned as-is
     * - null -> Expr.literal("NULL")
     * - Other types -> Expr.variable(value)
     */
    private fun toExpr(value: Any?): Expr = when (value) {
        is Expr -> value
        null -> Expr.literal("NULL")
        else -> Expr.variable(value)
    }
}

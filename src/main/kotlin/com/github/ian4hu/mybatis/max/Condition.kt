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

import kotlin.reflect.KProperty1

/**
 * Represents a conditional expression for SQL WHERE clauses.
 *
 * Conditions are expressions that evaluate to boolean values in SQL.
 * Use comparison operators (eq, ne, gt, etc.) or logical operators (and, or, not) to build conditions.
 */
interface Condition : Expr

/** Creates an equality condition: `a = b` */
infix fun Expr.eq(b: Expr): Condition = ComparisonOp.EqualTo.of(this, b)

inline infix fun <reified T> Expr.eq(b: KProperty1<T, *>) = ComparisonOp.EqualTo.of(this, Expr.kotlinProperty(b))

infix fun Expr.eq(b: Any): Condition = if (b is Expr) ComparisonOp.EqualTo.of(this, b) else ComparisonOp.EqualTo.of(this, Expr.variable(b))

/** Creates an inequality condition: `a <> b` */
fun Expr.ne(b: Expr): Condition = ComparisonOp.NotEqualTo.of(this, b)

/** Creates a greater-or-equal condition: `a >= b` */
fun Expr.ge(b: Expr): Condition = ComparisonOp.GreaterOrEqualTo.of(this, b)

/** Creates a greater-than condition: `a > b` */
fun Expr.gt(b: Expr): Condition = ComparisonOp.GreaterThan.of(this, b)

/** Creates a less-or-equal condition: `a <= b` */
fun Expr.le(b: Expr): Condition = ComparisonOp.LessOrEqualTo.of(this, b)

/** Creates a less-than condition: `a < b` */
fun Expr.lt(b: Expr): Condition = ComparisonOp.LessThan.of(this, b)

/** Creates a null-safe equality condition: `a <=> b` (returns true if both null) */
fun Expr.eqNullSafe(b: Expr): Condition = ComparisonOp.NullSafeEqualTo.of(this, b)

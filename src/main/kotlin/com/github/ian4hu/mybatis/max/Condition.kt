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

/**
 * @author ian
 * @date 2026/01/13
 */
interface Condition : Expr

fun Expr.eq(b: Expr): Condition = CompareOp.EqualTo.of(this, b)
fun Expr.ne(b: Expr): Condition = CompareOp.NotEqualTo.of(this, b)
fun Expr.ge(b: Expr): Condition = CompareOp.GreaterOrEqualTo.of(this, b)
fun Expr.gt(b: Expr): Condition = CompareOp.GreaterThan.of(this, b)
fun Expr.le(b: Expr): Condition = CompareOp.LessOrEqualTo.of(this, b)
fun Expr.lt(b: Expr): Condition = CompareOp.LessThan.of(this, b)
fun Expr.eqNullSafe(b: Expr): Condition = CompareOp.NullSafeEqualTo.of(this, b)

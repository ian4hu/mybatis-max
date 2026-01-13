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

import com.github.ian4hu.mybatis.max.conditions.DummyCondition
import com.github.ian4hu.mybatis.max.expr.SinglularBooleanExpr

/**
 * Unary boolean operators for SQL expressions.
 *
 * Includes logical NOT and SQL-specific predicates (IS NULL, IS TRUE, etc.).
 *
 * @property op the SQL operator keyword
 * @property inverseOp the inverse operator (NOT negates to DUMMY, IS NULL to IS NOT NULL, etc.)
 * @property prefix if true, operator appears before operand; otherwise after
 */
enum class SingularBooleanOp(val op: String, val inverseOp: String, val prefix: Boolean = false) {
    DUMMY("", "NOT", true),
    NOT("NOT", "DUMMY", true),
    IS_NULL("IS NULL", "IS_NOT_NULL"),
    IS_NOT_NULL("IS NOT NULL", "IS_NULL"),
    IS_TRUE("IS TRUE", "IS_NOT_TRUE"),
    IS_NOT_TRUE("IS NOT TRUE", "IS_RUE"),
    IS_FLASE("IS FALSE", "IS_NOT_FALSE"),
    IS_NOT_FALSE("IS NOT FALSE", "IS_FALSE"),
    IS_UNKNOWN("IS UNKNOWN", "IS_NOT_UNKNOWN"),
    IS_NOT_UNKNOWN("IS NOT UNKNOWN", "IS_UNKNOWN"),
    ;

    /** Creates a unary expression with this operator. */
    fun of(expr: Expr): Condition = SinglularBooleanExpr.of(this, expr)
}

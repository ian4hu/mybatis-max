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

import com.github.ian4hu.mybatis.exposed.conditions.ComparisonCondition

/**
 * SQL comparison operators for building conditional expressions.
 *
 * Supported operators:
 * - Equality: `=`, `<=>`
 * - Inequality: `<>`
 * - Relational: `>`, `>=`, `<`, `<=`
 *
 * @property op the SQL operator symbol
 */
enum class ComparisonOp(val op: String) {
    EqualTo("="),
    NullSafeEqualTo("<=>"),
    NotEqualTo("<>"),
    GreaterOrEqualTo(">="),
    LessOrEqualTo("<="),
    GreaterThan(">"),
    LessThan("<"),
    ;

    /** Creates a comparison condition with this operator. */
    fun of(a: Expr, b: Expr): Condition = ComparisonCondition(this, a, b)
}

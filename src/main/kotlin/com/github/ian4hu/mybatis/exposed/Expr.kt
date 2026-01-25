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

import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.exposed.conditions.DummyCondition
import com.github.ian4hu.mybatis.exposed.expr.ColumnExpr
import com.github.ian4hu.mybatis.exposed.expr.ConstantExpr
import com.github.ian4hu.mybatis.exposed.expr.FunctionCallExpr
import com.github.ian4hu.mybatis.exposed.expr.KotlinPropertyExpr
import com.github.ian4hu.mybatis.exposed.expr.LambdaExpr
import com.github.ian4hu.mybatis.exposed.expr.LiteralExpr
import com.github.ian4hu.mybatis.exposed.expr.VariableExpr
import kotlin.reflect.KProperty1

/**
 * Type-safe SQL expression that renders into SQL snippets.
 *
 * Expressions represent:
 * - **Column references**: String names, lambda references, or Kotlin properties
 * - **Variables**: MyBatis query parameters bound to PreparedStatement
 * - **Constants**: Primitives rendered as literals, objects as variables
 * - **Function calls**: Native SQL functions
 * - **Logical operations**: AND, OR, NOT operators
 *
 * Expressions compose to build complex, type-safe SQL conditions.
 */
interface Expr : Renderable {
    companion object {
        /**
         * Creates a column reference from a string-based column name.
         *
         * @param value the database column name
         * @return column expression
         */
        @JvmStatic fun column(value: String): Expr = ColumnExpr(value)

        /**
         * Creates a column reference from a MyBatis-Plus lambda method reference.
         *
         * Enables type-safe column references using Java getter methods (e.g., `User::getName`).
         *
         * @param I the entity type
         * @param O the property type
         * @param value the lambda method reference
         * @return column expression
         */
        @JvmStatic fun <I : Any, O> lambda(value: SFunction<I, O>): Expr = LambdaExpr(value)

        /**
         * Creates a column reference from a Kotlin property reference with reified type.
         *
         * Enables type-safe column references using Kotlin properties (e.g., `User::name`).
         *
         * @param T the entity type
         * @param value the Kotlin property reference
         * @return column expression
         */
        inline fun <reified T> kotlinProperty(value: KProperty1<T, *>): Expr = kotlinProperty(value, T::class.java)

        /**
         * Creates a column reference from a Kotlin property reference with explicit entity class.
         *
         * @param T the entity type
         * @param value the Kotlin property reference
         * @param entityClass the entity class for column name resolution
         * @return column expression
         */
        fun <T> kotlinProperty(
            value: KProperty1<T, *>,
            entityClass: Class<T>,
        ): Expr = KotlinPropertyExpr(value, entityClass)

        /**
         * Creates a literal expression that renders directly into SQL without escaping.
         *
         * Accepted literal types:
         * - **SQL identifiers**: Column names, table names, keywords (e.g., `user_name`, `COUNT`, `NULL`)
         * - **Numeric values**: Integers, decimals, scientific notation (e.g., `42`, `3.14`, `1.23E10`)
         *
         * **Valid**: `user_name`, `COUNT`, `42`, `3.14`, `NULL`
         *
         * **Invalid**: `user-name`, `1user`, `user name`, `user;DROP`, `'string'`
         *
         * @param value the raw SQL literal string
         * @return literal expression
         * @throws IllegalArgumentException if value doesn't match any safe literal pattern
         */
        @JvmStatic fun literal(value: String): Expr = LiteralExpr(value)

        /**
         * Creates a constant expression from a value.
         *
         * Primitives are rendered as SQL literals, objects as parameterized variables.
         *
         * @param value the constant value
         * @return constant expression
         */
        @JvmStatic fun constant(value: Any?): Expr = ConstantExpr(value)

        /**
         * Creates a variable expression bound as a MyBatis query parameter.
         *
         * Variables are safely parameterized in PreparedStatement to prevent SQL injection.
         *
         * @param value the parameter value
         * @param mapping optional MyBatis type handler mapping
         * @return variable expression
         */
        @JvmStatic
        fun variable(
            value: Any?,
            mapping: String? = null,
        ): VariableExpr = VariableExpr(value, mapping)

        /**
         * Creates a function call expression that renders as a native SQL function.
         *
         * Non-expression arguments are automatically wrapped as variable expressions.
         *
         * @param fn the SQL function name
         * @param args the function arguments (expressions or values)
         * @return function call expression
         * @throws IllegalArgumentException if any argument is an Alias
         */
        @JvmStatic
        fun functionCall(
            fn: String,
            vararg args: Any?,
        ): Expr = FunctionCallExpr(fn, *args)
    }

    /** Creates a NOT NULL condition: `a IS NOT NULL` */
    fun isNotNull(): Condition = isNull().not()

    /** Creates a NULL condition: `a IS NULL` */
    fun isNull(): Condition = UnaryBooleanOp.IS_NULL.of(this)

    /** Creates a boolean check condition: `IS TRUE`, `IS FALSE`, or `IS UNKNOWN` */
    fun isBool(bool: Boolean?): Condition = when (bool) {
        null -> UnaryBooleanOp.IS_UNKNOWN.of(this)
        true -> UnaryBooleanOp.IS_TRUE.of(this)
        else -> UnaryBooleanOp.IS_FLASE.of(this)
    }

    /** Creates a negated boolean check condition */
    fun isNotBool(bool: Boolean?): Condition = isBool(bool).not()

    fun asCondition(): Condition = DummyCondition.of(this)

    /** Creates an equality condition: `a = b` */
    fun equalTo(b: Expr): Condition = ComparisonOp.EqualTo.of(this, b)

}

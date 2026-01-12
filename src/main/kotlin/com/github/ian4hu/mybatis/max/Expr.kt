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

import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.max.expr.Alias
import com.github.ian4hu.mybatis.max.expr.AndExpr
import com.github.ian4hu.mybatis.max.expr.ColumnExpr
import com.github.ian4hu.mybatis.max.expr.ConstantExpr
import com.github.ian4hu.mybatis.max.expr.FunctionCallExpr
import com.github.ian4hu.mybatis.max.expr.KotlinPropertyExpr
import com.github.ian4hu.mybatis.max.expr.LambdaExpr
import com.github.ian4hu.mybatis.max.expr.LiteralExpr
import com.github.ian4hu.mybatis.max.expr.NotExpr
import com.github.ian4hu.mybatis.max.expr.OrExpr
import com.github.ian4hu.mybatis.max.expr.VariableExpr
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
         * - **Boolean values**: `true` or `false` (case-insensitive)
         *
         * **Valid**: `user_name`, `COUNT`, `42`, `3.14`, `true`, `NULL`
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
        ): Expr = VariableExpr(value, mapping)

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
        ): Expr = FunctionCallExpr(
            fn,
            *args
                .mapIndexed { index, it ->
                    if (it is Alias) {
                        throw IllegalArgumentException(
                            "Function parameter #$index: Alias can not as function parameter.",
                        )
                    }
                    it as? Expr ?: variable(it)
                }.toTypedArray(),
        )

        /**
         * Combines multiple expressions using AND.
         *
         * @param a the first expression
         * @param b the second expression
         * @param others additional expressions
         * @return AND expression
         */
        @JvmStatic fun and(
            a: Expr,
            b: Expr,
            vararg others: Expr,
        ): Expr = a.and(b, *others)

        /**
         * Combines multiple expressions using OR.
         *
         * @param a the first expression
         * @param b the second expression
         * @param others additional expressions
         * @return OR expression
         */
        @JvmStatic fun or(
            a: Expr,
            b: Expr,
            vararg others: Expr,
        ): Expr = a.or(b, *others)

        /**
         * Negates an expression using NOT.
         *
         * @param a the expression to negate
         * @return NOT expression
         */
        @JvmStatic fun not(a: Expr): Expr = a.not()
    }

    /**
     * Combines this expression with others using AND.
     *
     * Flattens nested ANDs and removes duplicates.
     *
     * @param b the second expression
     * @param expr additional expressions
     * @return AND expression, or single expression if optimized to one
     */
    fun and(b: Expr, vararg expr: Expr): Expr = AndExpr.of(this, b, *expr)

    /**
     * Combines this expression with others using OR.
     *
     * Flattens nested ORs and removes duplicates.
     *
     * @param b the second expression
     * @param expr additional expressions
     * @return OR expression, or single expression if optimized to one
     */
    fun or(b: Expr, vararg expr: Expr): Expr = OrExpr.of(this, b, *expr)

    /**
     * Negates this expression using NOT.
     *
     * Double negation is automatically eliminated.
     *
     * @return NOT expression, or original expression if already negated
     */
    fun not(): Expr = NotExpr.of(this)
}

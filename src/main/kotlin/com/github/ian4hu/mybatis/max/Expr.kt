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
 * Represents a type-safe SQL expression that can be rendered into SQL snippets.
 *
 * An expression can represent:
 * - **Column reference**: String-based column names, MyBatis-Plus lambda references ([SFunction]),
 *   or Kotlin property references
 * - **Variable**: MyBatis query parameter that will be bound to [java.sql.PreparedStatement]
 * - **Constant**: Primitive types rendered directly as SQL literals; non-primitive types treated as variables
 * - **Function call**: Native SQL function invocation
 * - **Logical operation**: Boolean expressions using AND, OR, or NOT operators
 *
 * Expressions compose together to build complex, type-safe SQL conditions for dynamic query construction.
 *
 * @param T the type of the expression's value
 */
interface Expr : Renderable {
    companion object {
        /**
         * Creates a column reference expression from a string-based column name.
         *
         * @param value the database column name
         * @return an expression representing the column
         */
        @JvmStatic fun column(value: String): Expr = ColumnExpr(value)

        /**
         * Creates a column reference expression from a MyBatis-Plus lambda method reference.
         *
         * This enables type-safe column references using Java getter methods, e.g., `User::getName`.
         *
         * @param I the entity type
         * @param O the property type
         * @param value the lambda method reference
         * @return an expression representing the column derived from the lambda
         */
        @JvmStatic fun <I : Any, O> lambda(value: SFunction<I, O>): Expr = LambdaExpr(value)

        /**
         * Creates a column reference expression from a Kotlin property reference with reified type.
         *
         * This enables type-safe column references using Kotlin properties, e.g., `User::name`.
         *
         * @param T the entity type
         * @param value the Kotlin property reference
         * @return an expression representing the column derived from the property
         */
        inline fun <reified T> kotlinProperty(value: KProperty1<T, *>): Expr = kotlinProperty(value, T::class.java)

        /**
         * Creates a column reference expression from a Kotlin property reference with explicit entity class.
         *
         * @param T the entity type
         * @param value the Kotlin property reference
         * @param entityClass the entity class for column name resolution
         * @return an expression representing the column derived from the property
         */
        fun <T> kotlinProperty(
            value: KProperty1<T, *>,
            entityClass: Class<T>,
        ): Expr = KotlinPropertyExpr(value, entityClass)

        /**
         * Creates a literal expression that renders directly into SQL without escaping or quoting.
         *
         * Literals are validated to ensure they contain only safe SQL values. Accepted literal types:
         * - **SQL identifiers**: Column names, table names, or keywords (e.g., `user_name`, `COUNT`, `NULL`)
         * - **Numeric values**: Integers, decimals, or scientific notation (e.g., `42`, `3.14`, `1.23E10`)
         * - **Boolean values**: `true` or `false` (case-insensitive)
         *
         * This validation prevents SQL injection attacks by rejecting potentially dangerous characters
         * and ensuring only recognized literal patterns are accepted.
         *
         * **Valid examples**: `user_name`, `COUNT`, `42`, `3.14`, `true`, `NULL`
         *
         * **Invalid examples**: `user-name`, `1user`, `user name`, `user;DROP`, `'string'`
         *
         * @param value the raw SQL literal string (must match a safe literal pattern)
         * @return an expression that renders as-is in SQL
         * @throws IllegalArgumentException if the value doesn't match any safe literal pattern
         */
        @JvmStatic fun literal(value: String): Expr = LiteralExpr(value)

        /**
         * Creates a constant expression from a value.
         *
         * Primitive types are rendered directly as SQL literals,
         * while non-primitive types are treated as parameterized variables.
         *
         * @param value the constant value
         * @return an expression representing the constant
         */
        @JvmStatic fun constant(value: Any?): Expr = ConstantExpr(value)

        /**
         * Creates a variable expression that will be bound as a MyBatis query parameter.
         *
         * Variables are safely parameterized in [java.sql.PreparedStatement] to prevent SQL injection.
         *
         * @param value the parameter value
         * @param mapping optional MyBatis type handler mapping
         * @return an expression representing the parameterized variable
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
         * @return an expression representing the function call
         * @throws IllegalArgumentException if any argument is an [Alias] expression
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
         * Combines multiple expressions using the AND logical operator.
         *
         * @param a the first expression
         * @param b the second expression
         * @param others additional expressions to combine
         * @return a composite AND expression
         */
        @JvmStatic fun and(
            a: Expr,
            b: Expr,
            vararg others: Expr,
        ): Expr = a.and(b, *others)

        /**
         * Combines multiple expressions using the OR logical operator.
         *
         * @param a the first expression
         * @param b the second expression
         * @param others additional expressions to combine
         * @return a composite OR expression
         */
        @JvmStatic fun or(
            a: Expr,
            b: Expr,
            vararg others: Expr,
        ): Expr = a.or(b, *others)

        /**
         * Negates an expression using the NOT logical operator.
         *
         * @param a the expression to negate
         * @return a NOT expression
         */
        @JvmStatic fun not(a: Expr): Expr = a.not()
    }

    /**
     * Combines this expression with others using the AND logical operator.
     *
     * Automatically flattens nested AND expressions and removes duplicates for optimization.
     * If only one distinct expression remains after flattening, returns that expression directly.
     *
     * @param expr additional expressions to combine with this one
     * @return a composite AND expression, or a single expression if only one remains
     */
    fun and(b: Expr, vararg expr: Expr): Expr = AndExpr.of(this, b, *expr)

    /**
     * Combines this expression with others using the OR logical operator.
     *
     * Automatically flattens nested OR expressions and removes duplicates for optimization.
     * If only one distinct expression remains after flattening, returns that expression directly.
     *
     * @param expr additional expressions to combine with this one
     * @return a composite OR expression, or a single expression if only one remains
     */
    fun or(b: Expr, vararg expr: Expr): Expr = OrExpr.of(this, b, *expr)

    /**
     * Negates this expression using the NOT logical operator.
     *
     * Applying NOT to an already negated expression returns the original expression (double negation elimination).
     *
     * @return a NOT expression, or the original expression if this is already a NOT expression
     */
    fun not(): Expr = NotExpr.of(this)
}

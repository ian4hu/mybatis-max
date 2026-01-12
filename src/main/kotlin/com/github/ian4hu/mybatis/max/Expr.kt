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
import com.github.ian4hu.mybatis.max.expr.Constant
import com.github.ian4hu.mybatis.max.expr.FunctionCallExpr
import com.github.ian4hu.mybatis.max.expr.KotlinPropertyExpr
import com.github.ian4hu.mybatis.max.expr.LambdaExpr
import com.github.ian4hu.mybatis.max.expr.LiteralExpr
import com.github.ian4hu.mybatis.max.expr.NotExpr
import com.github.ian4hu.mybatis.max.expr.OrExpr
import com.github.ian4hu.mybatis.max.expr.VariableExpr
import kotlin.reflect.KProperty1

/**
 * A SQL expression, can be
 * - a column : including String column, [lambda][SFunction], kotlin property
 * - variable : taking as Mybatis query parameters, finally be [java.sql.PreparedStatement]'s
 *   parameters
 * - constant : primitive types are direct rendered to SQL, non-primitive types will be treated as
 *   variable
 * - function call : render as native SQL function
 * - logical operation and/or/not : composite expression represent SQL boolean operation
 *
 *   Expression will render to SQL snippet, and be used
 */
interface Expr<T> : Renderable<T> {

    companion object {

        @JvmStatic
        fun column(value: String): Expr<String> = ColumnExpr(value)

        @JvmStatic
        fun <I, O> lambda(value: SFunction<I, O>): Expr<SFunction<*, *>> = LambdaExpr<I, O>(value)

        inline fun <reified T> kotlinProperty(value: KProperty1<T, *>): Expr<KProperty1<T, *>> =
            kotlinProperty(value, T::class.java)

        fun <T> kotlinProperty(value: KProperty1<T, *>, entityClass: Class<T>): Expr<KProperty1<T, *>> =
            KotlinPropertyExpr(value, entityClass)

        @JvmStatic
        fun literal(value: String): Expr<String> = LiteralExpr(value)

        @JvmStatic
        fun constant(value: Any?): Expr<*> = Constant(value)

        @JvmStatic
        fun variable(value: Any?, mapping: String? = null): Expr<*> = VariableExpr(value, mapping)

        @JvmStatic
        fun functionCall(fn: String, vararg args: Any?): Expr<*> =
            FunctionCallExpr(
                fn,
                *args
                    .mapIndexed { index, it ->
                        if (it is Alias<*>)
                            throw IllegalArgumentException(
                                "Function parameter #$index: Alias can not as function parameter."
                            )
                        it as? Expr<*> ?: variable(it)
                    }
                    .toTypedArray(),
            )

        @JvmStatic
        fun and(a: Expr<*>, b: Expr<*>, vararg others: Expr<*>): Expr<*> = a.and(b, *others)

        @JvmStatic
        fun or(a: Expr<*>, b: Expr<*>, vararg others: Expr<*>): Expr<*> = a.or(b, *others)

        @JvmStatic
        fun not(a: Expr<*>): Expr<*> = a.not()
    }

    fun and(vararg expr: Expr<*>): Expr<*> {
        val elements =
            listOf(this, *expr).flatMap { if (it is AndExpr) it.elements else listOf(it) }.distinct()
        if (elements.size == 1) {
            return elements[0]
        }
        return AndExpr<Any>(elements)
    }

    fun or(vararg expr: Expr<*>): Expr<*> {
        val elements =
            listOf(this, *expr).flatMap { if (it is OrExpr) it.elements else listOf(it) }.distinct()
        if (elements.size == 1) {
            return elements[0]
        }
        return OrExpr<Any>(elements)
    }

    fun not(): Expr<T> = if (this is NotExpr<T>) this.expr else NotExpr(this)
}


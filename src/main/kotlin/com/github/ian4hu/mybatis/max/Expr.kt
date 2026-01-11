package com.github.ian4hu.mybatis.max

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import kotlin.reflect.KProperty1

/**
 * A renderable object, which can be rendered with a MybatisPlus [com.baomidou.mybatisplus.core.conditions.Wrapper]
 */
interface Renderable<T> {
    fun render(wrapper: AbstractWrapper<*, *, *>): String

    fun alias(alias: String): Alias<T> = if(this is Alias) Alias(alias, this.expr) else Alias(alias, this)
}

/**
 * A SQL expression, can be
 *  - a column : including String column, [lambda][SFunction], kotlin property
 *  - variable : taking as Mybatis query parameters, finally be [java.sql.PreparedStatement]'s parameters
 *  - constant : primitive types are direct rendered to SQL, non-primitive types will be treated as variable
 *  - function call : render as native SQL function
 *  - logical operation and/or/not : composite expression represent SQL boolean operation
 *
 *  Expression will render to SQL snippet, and be used
 */
interface Expr<T>: Renderable<T> {

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
        fun functionCall(fn: String, vararg args: Any?): Expr<*> = FunctionCallExpr(fn, *args.mapIndexed { index, it ->
            if (it is Alias<*>) throw IllegalArgumentException("Function parameter #$index: Alias can not as function parameter.")
            it as? Expr<*> ?: variable(it)
        }.toTypedArray())

        @JvmStatic
        fun and(a: Expr<*>, b: Expr<*>, vararg others: Expr<*>): Expr<*> = a.and(b, *others)

        @JvmStatic
        fun or(a: Expr<*>, b: Expr<*>, vararg others: Expr<*>): Expr<*> = a.or(b, *others)

        @JvmStatic
        fun not(a: Expr<*>): Expr<*> = a.not()
    }

    fun and(vararg expr: Expr<*>): Expr<*> {
        val elements = listOf(this, *expr)
            .flatMap { if (it is AndExpr) it.elements else listOf(it) }
            .distinct()
        if (elements.size == 1) {
            return elements[0]
        }
        return AndExpr<Any>(elements)
    }

    fun or(vararg expr: Expr<*>): Expr<*> {
        val elements = listOf(this, *expr)
            .flatMap { if (it is OrExpr) it.elements else listOf(it) }
            .distinct()
        if (elements.size == 1) {
            return elements[0]
        }
        return OrExpr<Any>(elements)
    }

    fun not(): Expr<T> = if (this is NotExpr<T>) this.expr else NotExpr(this)
}

data class Alias<T>(val alias: String, val expr: Renderable<T>) : Renderable<T> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        val rendered = if (expr is CompositeExpr)  "(${expr.render(wrapper)})" else expr.render(wrapper)
        return if (alias.isBlank()) rendered else "$rendered AS $alias"
    }
}

/**
 * A composite expression, which need be bracketed in some situation
 */
interface CompositeExpr<T> : Expr<T>

data class OrExpr<T>(val elements: List<Expr<*>>) : CompositeExpr<T> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        return elements.distinct().joinToString(" OR ") {
            if (it is CompositeExpr<*> && it !is OrExpr<*>) "(${it.render(wrapper)})" else it.render(wrapper)
        }
    }
}

data class AndExpr<T>(val elements: List<Expr<*>>) : CompositeExpr<T> {

    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        return elements.distinct().joinToString(" AND ") {
            if (it is CompositeExpr<*> && it !is AndExpr<*>) "(${it.render(wrapper)})" else it.render(wrapper)
        }
    }
}

data class NotExpr<T>(val expr: Expr<T>) : CompositeExpr<T> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String =
        if (expr is CompositeExpr<T>) "NOT (${expr.render(wrapper)})" else "NOT ${expr.render(wrapper)}"
}

data class ColumnExpr(val value: String) : Expr<String> {
    override fun render(wrapper: AbstractWrapper<*, *, *>) : String = Helper.wrapColumn(wrapper, value)
}

data class LambdaExpr<out I, out O>(val value: SFunction<*, *>) : Expr<SFunction<*, *>> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapLambda(wrapper, value)
}

data class KotlinPropertyExpr<T>(val value: KProperty1<T, *>, val entityClass: Class<T>) : Expr<KProperty1<T, *>> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapProperty(wrapper, value, entityClass)
}

data class LiteralExpr(val value: String) : Expr<String> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = value
}

data class FunctionCallModel(val fn: String, val args: List<Expr<*>> = emptyList())

data class FunctionCallExpr(val value: FunctionCallModel) : Expr<FunctionCallModel> {

    constructor(fn: String, vararg args: Expr<*>) : this(FunctionCallModel(fn, listOf(*args)))

    override fun render(wrapper: AbstractWrapper<*, *, *>) : String {
        val symbol = Expr.literal(value.fn).render(wrapper)
        return value.args.joinToString(StringPool.COMMA, prefix = "${symbol}(", postfix = ")") {
            it.render(wrapper)
        }
    }
}

data class Constant(val value: Any?) : Expr<Any> {
    override fun render(wrapper: AbstractWrapper<*, *, *>) : String {
        if (value == null) {
            return Expr.literal("NULL").render(wrapper)
        }

        // Direct render primitive value
        if (isPrimitive(value) || isBoxedPrimitive(value)) {
            return Expr.literal(value.toString()).render(wrapper)
        }
        // Non primitive value will take as variable
        return Expr.variable(value).render(wrapper)
    }
}

data class VariableExpr(val value: Any?, val mapping: String? = null) : Expr<Any?> {
    override fun render(wrapper: AbstractWrapper<*, *, *>) : String {
        return Helper.wrapParam(wrapper, value, mapping)
    }
}

private fun isPrimitive(value: Any): Boolean {
    return value.javaClass.isPrimitive && value.javaClass != Void.TYPE
}

private fun isBoxedPrimitive(value: Any): Boolean {
    return when (value) {
        is Boolean -> true
        is Byte -> true
        is Short -> true
        is Int -> true
        is Long -> true
        is Float -> true
        is Double -> true
        else -> false
    }
}
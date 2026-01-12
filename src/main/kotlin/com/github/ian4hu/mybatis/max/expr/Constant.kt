package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

data class Constant(val value: Any?) : Expr<Any> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        if (value == null) {
            return Expr.Companion.literal("NULL").render(wrapper)
        }

        // Direct render primitive value
        if (isPrimitive(value) || isBoxedPrimitive(value)) {
            return Expr.Companion.literal(value.toString()).render(wrapper)
        }
        // Non primitive value will take as variable
        return Expr.Companion.variable(value).render(wrapper)
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
}
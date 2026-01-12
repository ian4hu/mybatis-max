package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.github.ian4hu.mybatis.max.Expr

data class FunctionCallExpr(val fn: String, val args: List<Expr<*>> = emptyList()) : Expr<Any> {

    constructor(fn: String, vararg args: Expr<*>) : this(fn, listOf(*args))

    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        val symbol = Expr.literal(fn).render(wrapper)
        return args.joinToString(StringPool.COMMA, prefix = "${symbol}(", postfix = ")") {
            it.render(wrapper)
        }
    }
}

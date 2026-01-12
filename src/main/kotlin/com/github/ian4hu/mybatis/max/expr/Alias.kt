package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Renderable

data class Alias<T>(val alias: String, val expr: Renderable<T>) : Renderable<T> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        val rendered = if (expr is CompositeExpr) "(${expr.render(wrapper)})" else expr.render(wrapper)
        return if (alias.isBlank()) rendered else "$rendered AS $alias"
    }
}


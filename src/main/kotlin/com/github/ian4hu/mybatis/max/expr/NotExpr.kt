package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

data class NotExpr<T>(val expr: Expr<T>) : CompositeExpr<T> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String =
        if (expr is CompositeExpr<T>) "NOT (${expr.render(wrapper)})"
        else "NOT ${expr.render(wrapper)}"
}
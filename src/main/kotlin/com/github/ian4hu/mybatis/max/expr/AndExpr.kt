package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

data class AndExpr<T>(val elements: List<Expr<*>>) : CompositeExpr<T> {

    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        return elements.distinct().joinToString(" AND ") {
            if (it is CompositeExpr<*> && it !is AndExpr<*>) "(${it.render(wrapper)})"
            else it.render(wrapper)
        }
    }
}
package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

data class LiteralExpr(val value: String) : Expr<String> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = value
}
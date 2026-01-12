package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.github.ian4hu.mybatis.max.Expr

data class ColumnExpr(val value: String) : Expr<String> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapColumn(wrapper, value)
}
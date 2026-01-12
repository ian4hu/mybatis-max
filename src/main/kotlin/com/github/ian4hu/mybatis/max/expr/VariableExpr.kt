package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.github.ian4hu.mybatis.max.Expr

data class VariableExpr(val value: Any?, val mapping: String? = null) : Expr<Any?> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        return Helper.wrapParam(wrapper, value, mapping)
    }
}
package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.max.Expr

data class LambdaExpr<out I, out O>(val value: SFunction<*, *>) : Expr<SFunction<*, *>> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapLambda(wrapper, value)
}
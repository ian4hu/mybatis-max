package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.github.ian4hu.mybatis.max.Expr
import kotlin.reflect.KProperty1

data class KotlinPropertyExpr<T>(val value: KProperty1<T, *>, val entityClass: Class<T>) :
    Expr<KProperty1<T, *>> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String =
        Helper.wrapProperty(wrapper, value, entityClass)
}
package com.github.ian4hu.mybatis.max

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.expr.Alias

/**
 * A renderable object, which can be rendered with a MybatisPlus
 * [com.baomidou.mybatisplus.core.conditions.Wrapper]
 */
interface Renderable<T> {
    fun render(wrapper: AbstractWrapper<*, *, *>): String

    fun alias(alias: String): Alias<T> =
        if (this is Alias) Alias(alias, this.expr) else Alias(alias, this)
}
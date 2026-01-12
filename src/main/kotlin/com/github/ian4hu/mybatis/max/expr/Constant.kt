/*
 *    Copyright 2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.ian4hu.mybatis.max.expr

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.Expr

data class Constant(
    val value: Any?,
) : Expr<Any> {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        if (value == null) {
            return Expr.Companion.literal("NULL").render(wrapper)
        }

        // Direct render primitive value
        if (isPrimitive(value) || isBoxedPrimitive(value)) {
            return Expr.Companion.literal(value.toString()).render(wrapper)
        }
        // Non primitive value will take as variable
        return Expr.Companion.variable(value).render(wrapper)
    }

    private fun isPrimitive(value: Any): Boolean = value.javaClass.isPrimitive && value.javaClass != Void.TYPE

    private fun isBoxedPrimitive(value: Any): Boolean =
        when (value) {
            is Boolean -> true
            is Byte -> true
            is Short -> true
            is Int -> true
            is Long -> true
            is Float -> true
            is Double -> true
            else -> false
        }
}

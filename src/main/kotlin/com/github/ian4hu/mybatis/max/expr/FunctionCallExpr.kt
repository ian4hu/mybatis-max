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
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Expr.Companion.variable

/**
 * SQL function call expression.
 *
 * Renders as `function_name(arg1, arg2, ...)` with validated function name.
 *
 * @property fn the SQL function name
 * @property args the function arguments
 */
data class FunctionCallExpr(
    val fn: String,
    val args: List<Expr> = emptyList(),
) : Expr {

    init {
        checkFunctionCall(this)
    }

    constructor(fn: String, vararg args: Expr) : this(fn, listOf(*args)) {
        checkFunctionCall(this)
    }

    constructor(fn: String, vararg args: Any?) : this(fn, *args
        .mapIndexed { index, it ->
            if (it is Alias) {
                throw IllegalArgumentException(
                    "Function parameter #$index: Alias can not as function parameter.",
                )
            }
            it as? Expr ?: variable(it)
        }.toTypedArray()) {
        checkFunctionCall(this)
    }

    override fun render(wrapper: AbstractWrapper<*, *, *>): String {
        val symbol = Expr.literal(fn).render(wrapper)
        return args.joinToString(StringPool.COMMA, prefix = "$symbol(", postfix = ")") {
            it.render(wrapper)
        }
    }

    companion object {
        fun checkFunctionCall(fn: FunctionCallExpr) {
            if (!Alias.isValidIdentifier(fn.fn)) throw IllegalArgumentException("Invalid function name: '$fn'")
        }
    }
}

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

import com.baomidou.mybatisplus.core.conditions.Helper
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render

/**
 * Column reference expression using string-based column name.
 *
 * The column name is validated and wrapped using MyBatis-Plus's column resolution.
 *
 * @property value the column name
 */
data class ColumnExpr(
    val value: String,
) : Expr {
    init {
        if (!Alias.isValidIdentifier(value)) throw IllegalArgumentException("'$value' is not valid column name")
    }
    override fun render(render: Render): String = render.column(value)
}

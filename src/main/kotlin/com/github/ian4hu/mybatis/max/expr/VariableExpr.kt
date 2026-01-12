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
import com.baomidou.mybatisplus.core.conditions.Helper
import com.github.ian4hu.mybatis.max.Expr

/**
 * Parameterized variable expression safely bound to PreparedStatement.
 *
 * Variables are the secure way to include dynamic values in SQL queries,
 * preventing SQL injection through proper parameterization.
 *
 * @property value the parameter value (can be null)
 * @property mapping optional MyBatis type handler mapping (e.g., `"jdbcType=VARCHAR"`)
 */
data class VariableExpr(
    val value: Any?,
    val mapping: String? = null,
) : Expr {
    /**
     * Renders this variable as a MyBatis parameter placeholder.
     *
     * @param wrapper the MyBatis-Plus wrapper context
     * @return a MyBatis parameter placeholder string
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapParam(wrapper, value, mapping)
}

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
 * Represents a parameterized variable expression that will be safely bound to [java.sql.PreparedStatement].
 *
 * Variables are the secure way to include dynamic values in SQL queries, as they are properly
 * parameterized through MyBatis-Plus's wrapper mechanism. This prevents SQL injection attacks
 * by ensuring values are never directly concatenated into SQL strings.
 *
 * The variable value is wrapped using MyBatis-Plus's [Helper.wrapParam] method, which generates
 * placeholder syntax (e.g., `#{param1}`) that MyBatis will replace with the actual value at
 * execution time.
 *
 * @property value the parameter value to be bound (can be null)
 * @property mapping optional MyBatis type handler mapping for custom type conversion (e.g., `"jdbcType=VARCHAR"`, `"typeHandler=MyTypeHandler"`)
 */
data class VariableExpr(
    val value: Any?,
    val mapping: String? = null,
) : Expr<Any?> {
    /**
     * Renders this variable as a MyBatis parameter placeholder.
     *
     * The wrapper's parameter list is updated with this variable's value, and a placeholder
     * reference (e.g., `#{ew.paramNameValuePairs.MPGENVAL1}`) is returned for inclusion in the SQL.
     *
     * @param wrapper the MyBatis-Plus wrapper context that manages parameter bindings
     * @return a MyBatis parameter placeholder string
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapParam(wrapper, value, mapping)
}

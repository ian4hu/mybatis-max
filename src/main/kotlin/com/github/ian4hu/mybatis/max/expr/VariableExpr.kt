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
    val mapping: Map<String, String> = emptyMap(),
) : Expr {
    constructor(value: Any?, mapping: String?) : this(value, parseMapping(mapping))
    /**
     * Renders this variable as a MyBatis parameter placeholder.
     *
     * @param wrapper the MyBatis-Plus wrapper context
     * @return a MyBatis parameter placeholder string
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapParam(wrapper, value, formatMapping(mapping))

    fun jdbcType(jdbcType: String) = mapping("jdbcType" to jdbcType)
    fun javaType(javaType: Class<*>) = mapping("javaType" to javaType.name)
    fun mode(mode: String) = mapping("mode" to mode)
    fun IN() = mode("IN")
    fun OUT() = mode("OUT")
    fun numericScale(scale: Int) = mapping("numericScale" to scale.toString())
    fun typeHandler(typeHandler: Class<*>) = mapping("typeHandler" to typeHandler.name)

    fun mapping(vararg kv: Pair<String, String>) = VariableExpr(value, mergeMapping(mapping, *kv))

    companion object {
        private fun parseMapping(mapping: String?): Map<String, String> {
            return mapping?.splitToSequence(',').orEmpty()
                .map { it.split('=',limit = 2) }
                .map { it[0].trim() to it[1].trim() }
                .toMap()
        }

        private fun formatMapping(mapping: Map<String, String>): String? {
            if (mapping.isEmpty()) return null
            return mapping.entries.joinToString(",") { (key, value) -> "$key=$value" }
        }

        private fun mergeMapping(mapping: Map<String, String>, vararg kv: Pair<String, String>) = mapping.plus(kv)
    }
}

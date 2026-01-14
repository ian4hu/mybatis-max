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
package com.github.ian4hu.mybatis.exposed.expr

import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.Render

/**
 * Parameterized variable expression safely bound to PreparedStatement.
 *
 * Variables are the secure way to include dynamic values in SQL queries,
 * preventing SQL injection through proper parameterization.
 *
 * Supports fluent configuration of MyBatis type mappings (jdbcType, javaType, mode, etc.).
 *
 * @property value the parameter value (can be null)
 * @property mapping MyBatis type handler mappings as key-value pairs
 */
data class VariableExpr(
    val value: Any?,
    val mapping: Map<String, String> = emptyMap(),
) : Expr {
    /** Secondary constructor for backward compatibility with string-based mapping. */
    constructor(value: Any?, mapping: String?) : this(value, parseMapping(mapping))

    override fun render(render: Render): String = render.formatParam(value, formatMapping(mapping))

    /** Configures JDBC type for this parameter. */
    fun jdbcType(jdbcType: String) = mapping("jdbcType" to jdbcType)

    /** Configures Java type for this parameter. */
    fun javaType(javaType: Class<*>) = mapping("javaType" to javaType.name)

    /** Configures parameter mode (IN, OUT, INOUT). */
    fun mode(mode: String) = mapping("mode" to mode)

    /** Sets parameter mode to IN. */
    fun modeIn() = mode("IN")

    /** Sets parameter mode to OUT. */
    fun modeOut() = mode("OUT")

    /** Configures numeric scale for decimal parameters. */
    fun numericScale(scale: Int) = mapping("numericScale" to scale.toString())

    /** Configures custom type handler for this parameter. */
    fun typeHandler(typeHandler: Class<*>) = mapping("typeHandler" to typeHandler.name)

    /** Adds or updates mapping entries, returning a new VariableExpr. */
    fun mapping(vararg kv: Pair<String, String>) = VariableExpr(value, mergeMapping(mapping, *kv))

    companion object {
        /** Parses string mapping (e.g., "jdbcType=VARCHAR,mode=IN") into a map. */
        private fun parseMapping(mapping: String?): Map<String, String> = mapping?.splitToSequence(',').orEmpty()
            .map { it.split('=', limit = 2) }
            .map { it[0].trim() to it[1].trim() }
            .toMap()

        /** Formats mapping map back to string for MyBatis. */
        private fun formatMapping(mapping: Map<String, String>): String? {
            if (mapping.isEmpty()) return null
            return mapping.entries.joinToString(",") { (key, value) -> "$key=$value" }
        }

        /** Merges new key-value pairs into existing mapping. */
        private fun mergeMapping(mapping: Map<String, String>, vararg kv: Pair<String, String>) = mapping.plus(kv)
    }
}

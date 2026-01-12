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
package com.github.ian4hu.mybatis.max

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.github.ian4hu.mybatis.max.expr.Alias

/**
 * Represents an object that can be rendered into SQL fragments.
 *
 * Implementations provide SQL string generation through the [Render] abstraction,
 * enabling integration with MyBatis-Plus dynamic SQL generation.
 */
interface Renderable {
    /**
     * Renders this object into a SQL fragment string.
     *
     * @param render the rendering context
     * @return the rendered SQL fragment
     */
    fun render(render: Render): String

    /**
     * Creates an aliased version of this renderable object for use in SQL queries.
     *
     * If this renderable is already an [Alias], replaces the alias name while preserving
     * the underlying expression. Otherwise, wraps this renderable in a new alias.
     *
     * @param alias the SQL alias name (e.g., "user_name" or "total_count")
     * @return an [Alias] wrapping this renderable with the specified alias name
     */
    fun alias(alias: String): Alias = if (this is Alias) Alias(alias, this.expr) else Alias(alias, this)
}

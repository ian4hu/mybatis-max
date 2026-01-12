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

import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import kotlin.reflect.KProperty1

/**
 * Abstraction for rendering SQL expressions into string representations.
 *
 * This interface decouples expression rendering from MyBatis-Plus's wrapper implementation,
 * enabling alternative rendering strategies and better testability.
 */
interface Render {

    /** Renders a column name to SQL. */
    fun column(column: String): String

    /** Renders a lambda method reference to SQL column name. */
    fun lambda(lambda: SFunction<*, *>): String

    /** Renders a Kotlin property reference to SQL column name. */
    fun <T> kotlinProperty(property: KProperty1<T, *>, entityClass: Class<T>): String

    /** Formats a parameter value with optional type mapping as MyBatis placeholder. */
    fun formatParam(param: Any?, mapping: String?): String
}

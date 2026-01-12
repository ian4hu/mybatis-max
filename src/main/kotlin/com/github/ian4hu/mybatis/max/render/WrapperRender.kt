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
package com.github.ian4hu.mybatis.max.render

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.conditions.Helper
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.max.Render
import kotlin.reflect.KProperty1

/**
 * MyBatis-Plus wrapper-based implementation of [Render].
 *
 * Delegates rendering operations to MyBatis-Plus's [Helper] utility methods,
 * providing integration with MyBatis-Plus's dynamic SQL generation.
 *
 * @property wrapper the MyBatis-Plus wrapper context for SQL generation
 */
class WrapperRender(val wrapper: AbstractWrapper<*, *, *>) : Render {
    override fun column(column: String): String = Helper.wrapColumn(wrapper, column)

    override fun lambda(lambda: SFunction<*, *>): String = Helper.wrapLambda(wrapper, lambda)

    override fun <T> kotlinProperty(property: KProperty1<T, *>, entityClass: Class<T>): String = Helper.wrapProperty(wrapper, property, entityClass)

    override fun formatParam(param: Any?, mapping: String?): String = Helper.wrapParam(wrapper, param, mapping)
}

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
import kotlin.reflect.KProperty1

/**
 * Column reference expression using Kotlin property reference.
 *
 * Enables type-safe column references in Kotlin using property syntax (e.g., `User::name`).
 *
 * @param T the entity type
 * @property value the Kotlin property reference
 * @property entityClass the entity class for column name resolution
 */
data class KotlinPropertyExpr<T>(
    val value: KProperty1<T, *>,
    val entityClass: Class<T>,
) : Expr {
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapProperty(wrapper, value, entityClass)
}

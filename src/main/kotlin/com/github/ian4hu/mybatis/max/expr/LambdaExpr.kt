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
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.Render

/**
 * Column reference expression using MyBatis-Plus lambda method references.
 *
 * Enables type-safe column references in Java using method references (e.g., `User::getName`).
 * For Kotlin code, consider using [KotlinPropertyExpr] with Kotlin property references.
 *
 * @param T the entity type
 * @property value the MyBatis-Plus serializable function reference
 */
data class LambdaExpr<T : Any>(
    val value: SFunction<T, *>,
) : Expr {
    /**
     * Renders this lambda reference as a SQL column name.
     *
     * Uses MyBatis-Plus's [Helper.wrapLambda] to extract the column name from the lambda.
     *
     * @param render the MyBatis-Plus wrapper context
     * @return the database column name
     */
    override fun render(render: Render): String = render.lambda(value)
}

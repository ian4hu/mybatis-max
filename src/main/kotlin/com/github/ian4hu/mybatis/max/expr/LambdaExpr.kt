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
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.github.ian4hu.mybatis.max.Expr

/**
 * Represents a type-safe column reference expression using MyBatis-Plus lambda method references.
 *
 * Lambda expressions enable compile-time type-safe column references in Java by using method references
 * to getter methods (e.g., `User::getName`). MyBatis-Plus analyzes these lambda references to extract
 * the corresponding database column names, providing refactoring-safe SQL generation.
 *
 * This expression type is particularly useful in Java environments where property references are not
 * available. For Kotlin code, consider using [KotlinPropertyExpr] with Kotlin property references instead.
 *
 * **Example usage**: `Expr.lambda(User::getName)` represents the column mapped to the `getName()` method.
 *
 * @param I the entity type (input)
 * @param O the property type (output/return type of the getter method)
 * @property value the MyBatis-Plus serializable function reference
 */
data class LambdaExpr<out I, out O>(
    val value: SFunction<*, *>,
) : Expr<SFunction<*, *>> {
    /**
     * Renders this lambda reference as a SQL column name.
     *
     * Uses MyBatis-Plus's [Helper.wrapLambda] to analyze the lambda method reference and extract
     * the corresponding database column name based on entity mapping configuration.
     *
     * @param wrapper the MyBatis-Plus wrapper context used for column name resolution
     * @return the database column name derived from the lambda reference
     */
    override fun render(wrapper: AbstractWrapper<*, *, *>): String = Helper.wrapLambda(wrapper, value)
}

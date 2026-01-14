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
package com.baomidou.mybatisplus.core.conditions

import com.baomidou.mybatisplus.core.conditions.query.Query
import com.baomidou.mybatisplus.core.toolkit.AopUtils
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.VarHandle
import kotlin.reflect.KProperty1

/**
 * Utility for invoking MyBatis-Plus wrapper internal methods via reflection.
 *
 * Provides bridge methods to access protected/internal MyBatis-Plus APIs,
 * enabling custom SQL expression rendering with proper column name resolution
 * and parameter handling.
 */
object Helper {
    /** To access the method [AbstractWrapper.columnToString] */
    private val COLUMN_TO_STRING: MethodHandle =
        MethodHandles
            .lookup()
            .findVirtual(
                AbstractWrapper::class.java,
                "columnToString",
                MethodType.methodType(String::class.java, Any::class.java),
            )

    /** Cache VarHandle by class */
    private val SQL_SELECT_HANDLES = mutableMapOf<Class<*>, VarHandle>()

    /**
     * Wraps a parameter value as a MyBatis placeholder and registers it in the wrapper.
     *
     * Delegates to [AbstractWrapper.formatParam] to generate parameter placeholders
     * (e.g., `#{ew.paramNameValuePairs.MPGENVAL1}`).
     *
     * @param wrapper the wrapper context for parameter registration
     * @param param the parameter value
     * @param mapping optional MyBatis type handler mapping (e.g., "jdbcType=VARCHAR")
     * @return SQL placeholder for the parameter
     */
    @JvmStatic
    fun wrapParam(
        wrapper: AbstractWrapper<*, *, *>,
        param: Any?,
        mapping: String? = null,
    ): String = wrapper.formatParam(mapping, param)

    /**
     * Resolves a string column name to its SQL representation.
     *
     * If the wrapper's column type matches string, uses the wrapper's entity mapping.
     * Otherwise, creates a temporary query wrapper for column resolution.
     *
     * @param wrapper the wrapper context
     * @param column the column name
     * @return the SQL column representation
     */
    @JvmStatic
    fun <T : Any> wrapColumn(
        wrapper: AbstractWrapper<*, T, *>,
        column: String,
    ): String {
        val target = if (matchColumnType(wrapper, column)) wrapper else Wrappers.query<T>()
        return invokeColumnToString(target, column)
    }

    /**
     * Resolves a lambda method reference to its SQL column representation.
     *
     * If the wrapper's column type matches lambda, uses the wrapper's entity mapping.
     * Otherwise, creates a temporary lambda query wrapper for column resolution.
     *
     * @param wrapper the wrapper context
     * @param column the lambda method reference (e.g., `User::getName`)
     * @return the SQL column representation
     */
    @JvmStatic
    fun <T : Any> wrapLambda(
        wrapper: AbstractWrapper<*, *, *>,
        column: SFunction<T, *>,
    ): String {
        val target = if (matchColumnType(wrapper, column)) wrapper else Wrappers.lambdaQuery<T>()
        return invokeColumnToString(target, column)
    }

    /**
     * Resolves a Kotlin property reference to its SQL column representation.
     *
     * If the wrapper's column type matches Kotlin property, uses the wrapper's entity mapping.
     * Otherwise, creates a temporary Kotlin query wrapper for column resolution.
     *
     * @param wrapper the wrapper context
     * @param column the Kotlin property reference (e.g., `User::name`)
     * @param entityClass the entity class for column name resolution
     * @return the SQL column representation
     */
    fun wrapProperty(
        wrapper: AbstractWrapper<*, *, *>,
        column: KProperty1<*, Any?>,
        entityClass: Class<*>,
    ): String {
        val target =
            if (matchColumnType(wrapper, column)) wrapper else KtQueryWrapper(entityClass = entityClass)
        return invokeColumnToString(target, column)
    }

    /**
     * Resolves a Kotlin property reference to its SQL column representation.
     *
     * Uses the wrapper's entity class for column name resolution.
     *
     * @param wrapper the wrapper context with entity class information
     * @param column the Kotlin property reference
     * @return the SQL column representation
     */
    fun <T : Any> wrapProperty(
        wrapper: AbstractWrapper<T, *, *>,
        column: KProperty1<T, Any?>,
    ): String = wrapProperty(wrapper, column, entityClass = wrapper.entityClass as Class<T>)

    /**
     * Checks if the column type matches the wrapper's expected column type.
     *
     * Resolves generic type arguments to determine if the provided column
     * is compatible with the wrapper's column type parameter.
     *
     * @param wrapper the wrapper to check
     * @param column the column value to match
     * @return true if column type matches wrapper's column type parameter
     */
    fun matchColumnType(
        wrapper: AbstractWrapper<*, *, *>,
        column: Any,
    ): Boolean {
        val typeArguments =
            GenericTypeUtils.resolveTypeArguments(wrapper.javaClass, AbstractWrapper::class.java)
        val columnType = typeArguments.get(1)
        return columnType.isInstance(column)
    }

    /**
     * Invokes the protected [AbstractWrapper.columnToString] method via reflection.
     *
     * @param target the wrapper instance
     * @param column the column value to convert
     * @return the SQL column string representation
     */
    fun invokeColumnToString(
        target: AbstractWrapper<*, *, *>,
        column: Any,
    ): String = COLUMN_TO_STRING.bindTo(target).invoke(column) as String

    /**
     * Retrieves the internal sqlSelect field from a query wrapper via reflection.
     *
     * Uses VarHandle for efficient field access, with caching for repeated access.
     *
     * @param wrapper the query wrapper
     * @return the shared string containing the SELECT clause
     */
    fun getSqlSelect(wrapper: Query<*, *, *>): SharedString {
        // Unwrap proxy
        val target = AopUtils.getTargetObject(wrapper)
        val queryType = target.javaClass
        val sqlSelectHandle =
            SQL_SELECT_HANDLES.getOrPut(queryType) {
                MethodHandles
                    .privateLookupIn(queryType, MethodHandles.lookup())
                    .findVarHandle(queryType, "sqlSelect", SharedString::class.java)
            }
        return sqlSelectHandle.get(target) as SharedString
    }
}

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
 * Helper to invoke Wrapper's internal method
 * @author ian
 * @date 2026/01/07
 */
object Helper {

    /**
     * To access the method [AbstractWrapper.columnToString]
     */
    private val COLUMN_TO_STRING: MethodHandle = MethodHandles.lookup()
        .findVirtual(
            AbstractWrapper::class.java,
            "columnToString",
            MethodType.methodType(String::class.java, Any::class.java)
        )

    /**
     * Cache VarHandle by class
     */
    private val SQL_SELECT_HANDLES = mutableMapOf<Class<*>, VarHandle>()

    /**
     * Invoke [AbstractWrapper.formatParam] to generate placeholder for a param, and add it to paramMap
     * @return SQL placeholder for param
     */
    @JvmStatic
    fun wrapParam(wrapper: AbstractWrapper<*, *, *>, param: Any?, mapping: String? = null): String {
        return wrapper.formatParam(mapping, param)
    }

    @JvmStatic
    fun <T : Any> wrapColumn(wrapper: AbstractWrapper<*, T, *>, column: String): String {
        val target = if (matchColumnType(wrapper, column)) wrapper else Wrappers.query<T>()
        return invokeColumnToString(target, column)
    }

    @JvmStatic
    fun <T : Any> wrapLambda(wrapper: AbstractWrapper<*, *, *>, column: SFunction<T, *>): String {
        val target = if (matchColumnType(wrapper, column)) wrapper else Wrappers.lambdaQuery<T>()
        return invokeColumnToString(target, column)
    }

    fun wrapProperty(
        wrapper: AbstractWrapper<*, *, *>,
        column: KProperty1<*, Any?>,
        entityClass: Class<*>
    ): String {
        val target = if (matchColumnType(wrapper, column)) wrapper else KtQueryWrapper(entityClass = entityClass)
        return invokeColumnToString(target, column)
    }

    fun <T : Any> wrapProperty(wrapper: AbstractWrapper<T, *, *>, column: KProperty1<T, Any?>): String {
        return wrapProperty(wrapper, column, entityClass = wrapper.entityClass as Class<T>)
    }

    fun matchColumnType(wrapper: AbstractWrapper<*, *, *>, column: Any): Boolean {
        val typeArguments = GenericTypeUtils.resolveTypeArguments(wrapper.javaClass, AbstractWrapper::class.java)
        val columnType = typeArguments.get(1)
        return columnType.isInstance(column)
    }

    fun invokeColumnToString(target: AbstractWrapper<*, *, *>, column: Any): String {
        return COLUMN_TO_STRING.bindTo(target).invoke(column) as String
    }

    fun getSqlSelect(wrapper: Query<*, *, *>): SharedString {
        // Unwrap proxy
        val target = AopUtils.getTargetObject(wrapper)
        val clazz = target.javaClass
        val sqlSelectHandle = SQL_SELECT_HANDLES.getOrPut(clazz) {
            MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                .findVarHandle(clazz, "sqlSelect", SharedString::class.java)
        }
        return sqlSelectHandle.get(target) as SharedString
    }
}

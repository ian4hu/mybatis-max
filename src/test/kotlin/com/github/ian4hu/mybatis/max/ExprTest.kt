package com.github.ian4hu.mybatis.max

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO
import com.github.ian4hu.mybatis.max.mapper.BlockStorageMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.Double
import java.util.stream.Stream
import kotlin.Any
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.test.assertEquals
import kotlin.to


class ExprTest {
    @Test
    fun render() {
        MybatisConfiguration().addMapper(BlockStorageMapper::class.java)
        val wrapper = Wrappers.query<Any>()
        val concat = Expr.functionCall("concat", "A", "B", "C", null).render(wrapper)
        Assertions.assertEquals(
            "concat(#{ew.paramNameValuePairs.MPGENVAL1},#{ew.paramNameValuePairs.MPGENVAL2},#{ew.paramNameValuePairs.MPGENVAL3},#{ew.paramNameValuePairs.MPGENVAL4})",
            concat
        )
        Assertions.assertEquals("A", wrapper.paramNameValuePairs["MPGENVAL1"])
        Assertions.assertEquals("B", wrapper.paramNameValuePairs["MPGENVAL2"])
        Assertions.assertEquals("C", wrapper.paramNameValuePairs["MPGENVAL3"])
        Assertions.assertNull(wrapper.paramNameValuePairs["MPGENVAL4"])

        val currentTimestamp = Expr.functionCall("current_timestamp").render(Wrappers.query<Any>())
        Assertions.assertEquals("current_timestamp()", currentTimestamp)

        val functionCall = Expr.functionCall(
            "wm_concat",
            Expr.Companion.constant(1),
            Expr.Companion.lambda(JavaHelperTest.metadata()),
            Expr.Companion.constant(true), Expr.Companion.constant("A"),
            Expr.Companion.kotlinProperty(BlockStorageDBO::id),
            Expr.Companion.column("out_biz_id"),
            Expr.Companion.constant(null),
            Constant(Double.valueOf("10"))
        )
        val wmConcat = functionCall.render(Wrappers.query<Any>())
        val exprStr = "wm_concat(1,metadata,true,#{ew.paramNameValuePairs.MPGENVAL1},id,out_biz_id,NULL,10.0)"
        Assertions.assertEquals(exprStr, wmConcat)
        Assertions.assertEquals(exprStr, functionCall.render(Wrappers.query<Any>()))
        val aliasedFunctionCall = functionCall.alias("expr")
        Assertions.assertEquals("${exprStr} AS expr", aliasedFunctionCall.render(Wrappers.query<Any>()))
    }

    @Test
    fun composite() {
        val andExpr = Expr.and(
            Expr.Companion.literal("A").and(Expr.Companion.literal("A")),
            Expr.Companion.literal("B"),
            Expr.Companion.literal("C")
        )
        val orExpr = Expr.or(
            Expr.Companion.literal("C").or(Expr.Companion.literal("C")),
            Expr.Companion.literal("D"),
            Expr.Companion.literal("E")
        )

        val expr = Expr.not(Expr.and(andExpr, andExpr, Expr.or(orExpr, orExpr, andExpr))).not().not()

        val exprStr = expr.render(Wrappers.query<Any>())
        Assertions.assertEquals("NOT (A AND B AND C AND (C OR D OR E OR (A AND B AND C)))", exprStr)
        val alias = expr.alias("expr")
        Assertions.assertEquals("($exprStr) AS expr", alias.render(Wrappers.query<Any>()))
    }

    companion object {

        @JvmStatic
        fun wrappersProvider(): Stream<Arguments> {
            MybatisConfiguration().addMapper(BlockStorageMapper::class.java)

            return Stream.of(
                "QueryWrapper" to Wrappers.query<Any>(),
                "LambdaQueryWrapper" to Wrappers.lambdaQuery(),
                "KtQueryWrapper" to KtQueryWrapper(BlockStorageDBO::class.java),
            ).map { Arguments.of(it.first, it.second) }
        }

        @JvmStatic
        fun functionCallProvider() : Stream<Arguments> {
            return Stream.of(
                Arguments.of("concat", listOf(Expr.literal("id"), Expr.variable("p0")), "concat(id,#{ew.paramNameValuePairs.MPGENVAL1})"),
                Arguments.of("current_timestamp", listOf(Expr.constant(6)), "current_timestamp(6)"),
                Arguments.of("wm_concat", listOf(",", Expr.column("id")), "wm_concat(#{ew.paramNameValuePairs.MPGENVAL1},id)")
            ).flatMap { fn ->
                wrappersProvider().map { Arguments.of("${fn.get()[0]} - ${it.get()[0]}",*it.get().drop(1).toTypedArray(), *fn.get()) }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("wrappersProvider")
    fun testKotlinProperty(name: String, wrapper: AbstractWrapper<*, *, *>) {
        val properties = mapOf(
            BlockStorageDBO::id to "id",
            BlockStorageDBO::outBizId to "out_biz_id",
            BlockStorageDBO::buffSize to "buff_size",
        )


        for ((k,v) in properties) {
            val result = Expr.kotlinProperty(k).render(wrapper)
            assertEquals(v, result)
        }
    }

    @ParameterizedTest
    @MethodSource("wrappersProvider")
    fun testLiteral(name: String, wrapper: AbstractWrapper<*, *, *>) {
        val literal = "current_timestamp"
        val result = Expr.literal(literal).render(wrapper)
        assertEquals(literal, result)
    }

    @ParameterizedTest
    @MethodSource("wrappersProvider")
    fun testVariable(name: String, wrapper: AbstractWrapper<*, *, *>) {
        val result = Expr.variable(name).render(wrapper)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result)
        assertEquals(name, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @ParameterizedTest
    @MethodSource("functionCallProvider")
    fun testFunctionCall(name: String, wrapper: AbstractWrapper<*, *, *>, fn: String, args: List<Any?>, expected: String) {
        val result = Expr.functionCall(fn, *args.toTypedArray()).render(wrapper)
        assertEquals(expected, result)
    }

    @Test
    fun testFunctionCallWithAlias() {
        val exception = assertThrows<IllegalArgumentException> {
            Expr.functionCall("fn", Expr.column("name").alias("nick"))
        }
        assertEquals("Function parameter #0: Alias can not as function parameter.", exception.message)
    }
}
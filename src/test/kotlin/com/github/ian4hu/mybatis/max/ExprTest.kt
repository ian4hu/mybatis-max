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
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.github.ian4hu.mybatis.max.Expr.Companion.and
import com.github.ian4hu.mybatis.max.Expr.Companion.column
import com.github.ian4hu.mybatis.max.Expr.Companion.constant
import com.github.ian4hu.mybatis.max.Expr.Companion.functionCall
import com.github.ian4hu.mybatis.max.Expr.Companion.kotlinProperty
import com.github.ian4hu.mybatis.max.Expr.Companion.lambda
import com.github.ian4hu.mybatis.max.Expr.Companion.literal
import com.github.ian4hu.mybatis.max.Expr.Companion.not
import com.github.ian4hu.mybatis.max.Expr.Companion.or
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO
import com.github.ian4hu.mybatis.max.expr.AndExpr
import com.github.ian4hu.mybatis.max.expr.ConstantExpr
import com.github.ian4hu.mybatis.max.expr.OrExpr
import org.apache.ibatis.type.BigIntegerTypeHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import java.lang.Double
import java.util.stream.Stream
import kotlin.Any
import kotlin.IllegalArgumentException
import kotlin.String
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.to

class ExprTest : MybatisBootstrap {
    @Test
    fun render() {
        val wrapper = Wrappers.query<Any>()
        val concat = functionCall("concat", "A", "B", "C", null).render(wrapper)
        Assertions.assertEquals(
            "concat(#{ew.paramNameValuePairs.MPGENVAL1},#{ew.paramNameValuePairs.MPGENVAL2},#{ew.paramNameValuePairs.MPGENVAL3},#{ew.paramNameValuePairs.MPGENVAL4})",
            concat,
        )
        Assertions.assertEquals("A", wrapper.paramNameValuePairs["MPGENVAL1"])
        Assertions.assertEquals("B", wrapper.paramNameValuePairs["MPGENVAL2"])
        Assertions.assertEquals("C", wrapper.paramNameValuePairs["MPGENVAL3"])
        Assertions.assertNull(wrapper.paramNameValuePairs["MPGENVAL4"])

        val currentTimestamp = functionCall("current_timestamp").render(Wrappers.query<Any>())
        Assertions.assertEquals("current_timestamp()", currentTimestamp)

        val functionCall =
            functionCall(
                "wm_concat",
                constant(1),
                lambda(JavaHelperTest.metadata()),
                constant(true),
                constant("A"),
                kotlinProperty(BlockStorageDBO::id),
                column("out_biz_id"),
                constant(null),
                ConstantExpr(Double.valueOf("10")),
            )
        val wmConcat = functionCall.render(Wrappers.query<Any>())
        val exprStr =
            "wm_concat(1,metadata,true,A,id,out_biz_id,NULL,10.0)"
        Assertions.assertEquals(exprStr, wmConcat)
        Assertions.assertEquals(exprStr, functionCall.render(Wrappers.query<Any>()))
        val aliasedFunctionCall = functionCall.alias("expr")
        Assertions.assertEquals("$exprStr AS expr", aliasedFunctionCall.render(Wrappers.query<Any>()))
    }

    @Test
    fun composite() {
        val andExpr = and(literal("A").and(literal("A")), literal("B"), literal("C"))
        val orExpr = or(literal("C").or(literal("C")), literal("D"), literal("E"))

        val expr = not(and(andExpr, andExpr, or(orExpr, orExpr, andExpr))).not().not()

        val exprStr = expr.render(Wrappers.query<Any>())
        Assertions.assertEquals("NOT (A AND B AND C AND (C OR D OR E OR (A AND B AND C)))", exprStr)
        val alias = expr.alias("expr")
        Assertions.assertEquals("($exprStr) AS expr", alias.render(Wrappers.query<Any>()))
    }

    companion object {
        @JvmStatic
        fun wrappersProvider(): Stream<Arguments> = Stream
            .of(
                "QueryWrapper" to Wrappers.query<Any>(),
                "LambdaQueryWrapper" to Wrappers.lambdaQuery(),
                "KtQueryWrapper" to KtQueryWrapper(BlockStorageDBO::class.java),
            ).map { of(it.first, it.second) }

        @JvmStatic
        fun functionCallProvider(): Stream<Arguments> = Stream
            .of(
                of(
                    "concat",
                    listOf(literal("id"), Expr.variable("p0")),
                    "concat(id,#{ew.paramNameValuePairs.MPGENVAL1})",
                ),
                of("current_timestamp", listOf(constant(6)), "current_timestamp(6)"),
                of(
                    "wm_concat",
                    listOf(",", column("id")),
                    "wm_concat(#{ew.paramNameValuePairs.MPGENVAL1},id)",
                ),
            ).flatMap { fn ->
                wrappersProvider().map {
                    of("${fn.get()[0]} - ${it.get()[0]}", *it.get().drop(1).toTypedArray(), *fn.get())
                }
            }

        @JvmStatic
        fun exprTestSource(): Stream<Arguments> = Stream
            .of(
                of(and(literal("A"), literal("B"), literal("C")), "A AND B AND C"),
                of(or(literal("A"), literal("B"), literal("C")), "A OR B OR C"),
                of(not(or(literal("A"), literal("B"), literal("C"))), "NOT (A OR B OR C)"),
                of(not(not(literal("A"))), "A"),
                of(not(literal("A")), "NOT A"),
                of(
                    and(and(literal("A"), literal("B")), and(literal("B"), literal("C"))),
                    "A AND B AND C",
                ),
                of(
                    AndExpr.of(
                        and(literal("A"), literal("B")),
                        and(literal("B"), literal("C")),
                    ),
                    "A AND B AND C",
                ),
                of(or(or(literal("A"), literal("B")), or(literal("B"), literal("C"))), "A OR B OR C"),
                of(
                    OrExpr.of(
                        or(literal("A"), literal("B")),
                        or(literal("B"), literal("C")),
                    ),
                    "A OR B OR C",
                ),
                of(
                    and(or(literal("A"), literal("B")), or(literal("B"), literal("C"))),
                    "(A OR B) AND (B OR C)",
                ),
                of(
                    and(not(or(literal("A"), literal("B"))), or(literal("B"), literal("C"))),
                    "(NOT (A OR B)) AND (B OR C)",
                ),
                of(
                    or(and(literal("A"), literal("B")), and(literal("B"), literal("C"))),
                    "(A AND B) OR (B AND C)",
                ),
                of(
                    or(not(and(literal("A"), literal("B"))), and(literal("B"), literal("C"))),
                    "(NOT (A AND B)) OR (B AND C)",
                ),
                of(lambda(JavaHelperTest.metadata()), "metadata"),
                of(kotlinProperty(BlockStorageDBO::outBizId), "out_biz_id"),
                of(functionCall("concat", literal("A"), literal("B")), "concat(A,B)"),
                of(column("id").alias("aid"), "id AS aid"),
                of(column("id").alias("aid").alias("bid"), "id AS bid"),
                of(column("id").alias("aid").alias(""), "id"),
                of(constant(1.toShort()).alias("aid"), "1 AS aid"),
                of(and(literal("+1"),literal("-1"), literal("TRUE"), literal("NULL")), "+1 AND -1 AND TRUE AND NULL")
            ).flatMap { expr ->
                wrappersProvider().map {
                    of(
                        "${expr.get()[0].javaClass.simpleName} - ${it.get()[0]}",
                        *it.get().drop(1).toTypedArray(),
                        *expr.get(),
                    )
                }
            }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrappersProvider")
    fun testKotlinProperty(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
    ) {
        val properties =
            mapOf(
                BlockStorageDBO::id to "id",
                BlockStorageDBO::outBizId to "out_biz_id",
                BlockStorageDBO::buffSize to "buff_size",
            )

        for ((k, v) in properties) {
            val result = kotlinProperty(k).render(wrapper)
            assertEquals(v, result)
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrappersProvider")
    fun testLiteral(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
    ) {
        val literal = "current_timestamp"
        val result = literal(literal).render(wrapper)
        assertEquals(literal, result)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrappersProvider")
    fun testInvalidLiteral(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
    ) {
        assertFailsWith(IllegalArgumentException::class) {
            val literal = "A';DROP"
            literal(literal).render(wrapper)
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wrappersProvider")
    fun testVariable(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
    ) {
        val result = Expr.variable(name).render(wrapper)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result)
        assertEquals(name, wrapper.paramNameValuePairs["MPGENVAL1"])

        val varWithJdbcType = Expr.variable(name, "jdbcType=VARCHAR").render(wrapper)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL2,jdbcType=VARCHAR}", varWithJdbcType)

        val varWithMapping = Expr.variable(name)
            .jdbcType("BIGINT")
            .javaType(Long::class.java)
            .typeHandler(BigIntegerTypeHandler::class.java)
            .OUT()
            .IN()
            .numericScale(6)
            .render(wrapper)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL3,jdbcType=BIGINT,javaType=long,typeHandler=org.apache.ibatis.type.BigIntegerTypeHandler,mode=IN,numericScale=6}", varWithMapping)

        val varWithStringMapping = Expr.variable(name, "jdbcType=BIGINT,javaType=long,typeHandler=org.apache.ibatis.type.BigIntegerTypeHandler,mode=IN,numericScale=6")
            .render(wrapper)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL4,jdbcType=BIGINT,javaType=long,typeHandler=org.apache.ibatis.type.BigIntegerTypeHandler,mode=IN,numericScale=6}", varWithStringMapping)
    }

    @ParameterizedTest(name = "{0} - {4}")
    @MethodSource("functionCallProvider")
    fun testFunctionCall(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
        fn: String,
        args: List<Any?>,
        expected: String,
    ) {
        val result = functionCall(fn, *args.toTypedArray()).render(wrapper)
        assertEquals(expected, result)
    }

    @Test
    fun testFunctionCallWithAlias() {
        val exception =
            assertThrows<IllegalArgumentException> {
                functionCall("fn", column("name").alias("nick"))
            }
        assertEquals("Function parameter #0: Alias can not as function parameter.", exception.message)
    }

    @Test
    fun testFunctionWithUnsafeLiteral() {
        assertFailsWith<IllegalArgumentException> {
            functionCall("--fn", column("name"))
        }
    }

    @Test
    fun testColumnWithUnsafeLiteral() {
        assertFailsWith<IllegalArgumentException> {
            column("A as B")
        }
    }

    @Test
    fun testAliasWithUnsafeLiteral() {
        assertFailsWith<IllegalArgumentException> {
            column("name").alias(";nick")
        }
    }

    @ParameterizedTest(name = "{0} - {3}")
    @MethodSource("exprTestSource")
    fun testVariousExpression(
        name: String,
        wrapper: AbstractWrapper<*, *, *>,
        expr: Renderable,
        expected: String,
    ) {
        val result = expr.render(wrapper)
        assertEquals(expected, result)
    }
}

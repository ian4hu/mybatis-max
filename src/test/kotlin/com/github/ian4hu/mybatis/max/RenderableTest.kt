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

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO
import com.github.ian4hu.mybatis.max.render.WrapperRender
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test for Renderable interface and alias functionality.
 */
class RenderableTest : MybatisBootstrap {

    @Test
    fun testSimpleAlias() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test column with alias
        val aliased = Expr.column("user_id").alias("uid")
        val result = aliased.render(render)
        assertEquals("user_id AS uid", result)
    }

    @Test
    fun testEmptyAlias() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Empty alias should render without AS
        val aliased = Expr.column("name").alias("")
        val result = aliased.render(render)
        assertEquals("name", result)

        // Blank alias should also render without AS
        val blankAliased = Expr.column("age").alias("   ")
        // This should throw exception as blank is not valid identifier
        // But empty string is allowed and returns expression without alias
    }

    @Test
    fun testAliasReplacement() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that calling alias on an already aliased expression replaces the alias
        val first = Expr.column("id").alias("user_id")
        val second = first.alias("uid")
        val result = second.render(render)
        assertEquals("id AS uid", result)

        // Test multiple replacements
        val third = second.alias("identity")
        val finalResult = third.render(render)
        assertEquals("id AS identity", finalResult)
    }

    @Test
    fun testAliasWithFunctionCall() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test function call with alias
        val func = Expr.functionCall("COUNT", Expr.column("id"))
        val aliased = func.alias("total")
        val result = aliased.render(render)
        assertEquals("COUNT(id) AS total", result)

        // Test complex function with alias
        val complexFunc = Expr.functionCall("CONCAT", Expr.column("first_name"), Expr.constant(" "), Expr.column("last_name"))
        val complexAliased = complexFunc.alias("full_name")
        val complexResult = complexAliased.render(render)
        assertEquals("CONCAT(first_name,#{ew.paramNameValuePairs.MPGENVAL1},last_name) AS full_name", complexResult)
    }

    @Test
    fun testAliasWithCompositeExpression() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that composite expressions are wrapped in parentheses when aliased
        val composite = Expr.literal("A").and(Expr.literal("B"))
        val aliased = composite.alias("condition")
        val result = aliased.render(render)
        assertEquals("(A AND B) AS condition", result)

        // Test OR expression
        val orComposite = Expr.literal("X").or(Expr.literal("Y"))
        val orAliased = orComposite.alias("choice")
        val orResult = orAliased.render(render)
        assertEquals("(X OR Y) AS choice", orResult)
    }

    @Test
    fun testAliasWithConstant() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test constant with alias
        val constant = Expr.constant(100).alias("score")
        val result = constant.render(render)
        assertEquals("100 AS score", result)

        // Test null constant with alias
        val nullConstant = Expr.constant(null).alias("empty")
        val nullResult = nullConstant.render(render)
        assertEquals("NULL AS empty", nullResult)
    }

    @Test
    fun testAliasWithVariable() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test variable with alias
        val variable = Expr.variable("dynamic_value").alias("value")
        val result = variable.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1} AS value", result)
    }

    @Test
    fun testAliasWithLiteral() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test literal with alias
        val literal = Expr.literal("STATUS").alias("current_status")
        val result = literal.render(render)
        assertEquals("STATUS AS current_status", result)
    }

    @Test
    fun testAliasWithLambda() {
        val wrapper = Wrappers.query<BlockStorageDBO>()
        val render = WrapperRender(wrapper)

        // Test lambda expression with alias
        val lambda = Expr.lambda(JavaHelperTest.metadata())
        val aliased = lambda.alias("meta")
        val result = aliased.render(render)
        assertEquals("metadata AS meta", result)
    }

    @Test
    fun testAliasWithKotlinProperty() {
        val wrapper = Wrappers.query<BlockStorageDBO>()
        val render = WrapperRender(wrapper)

        // Test Kotlin property with alias
        val property = Expr.kotlinProperty(BlockStorageDBO::buffSize)
        val aliased = property.alias("buffer_size")
        val result = aliased.render(render)
        assertEquals("buff_size AS buffer_size", result)
    }

    @Test
    fun testAliasWithComparison() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test comparison condition with alias (wraps in parentheses)
        val comparison = Expr.column("age").ge(Expr.constant(18))
        val aliased = comparison.alias("is_adult")
        val result = aliased.render(render)
        // Comparison is not composite, so no parentheses
        assertEquals("age>=18 AS is_adult", result)
    }

    @Test
    fun testAliasWithNestedComposite() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test deeply nested composite expression
        val inner = Expr.literal("A").and(Expr.literal("B"))
        val middle = inner.or(Expr.literal("C"))
        val outer = middle.and(Expr.literal("D"))
        val aliased = outer.alias("complex")
        val result = aliased.render(render)
        assertEquals("(((A AND B) OR C) AND D) AS complex", result)
    }

    @Test
    fun testRenderableInterface() {
        // Test that all expression types implement Renderable
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val renderables = listOf(
            Expr.column("id"),
            Expr.literal("VALUE"),
            Expr.constant(42),
            Expr.variable("param"),
            Expr.functionCall("NOW"),
            Expr.literal("A").and(Expr.literal("B")),
            Expr.literal("X").or(Expr.literal("Y")),
            Expr.literal("Z").not(),
            Expr.column("age").eq(Expr.constant(18)),
        )

        renderables.forEach { renderable ->
            assertNotNull(renderable)
            assertTrue(renderable is Renderable)
            val result = renderable.render(render)
            assertNotNull(result)
            assertTrue(result.isNotEmpty())
        }
    }

    @Test
    fun testAliasChainWithDifferentExpressionTypes() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Start with column, add operations, then alias
        val expr = Expr.column("value")
            .eq(Expr.constant(10))
            .and(Expr.column("status").eq(Expr.literal("OK")))
            .alias("is_valid")

        val result = expr.render(render)
        assertEquals("(value=10 AND status=OK) AS is_valid", result)
    }

    @Test
    fun testAliasPreservesOriginalExpression() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Verify that the original expression is preserved when creating alias
        val original = Expr.column("name")
        val aliased = original.alias("user_name")

        // Original should still render without alias
        assertEquals("name", original.render(render))

        // Aliased version should have alias
        assertEquals("name AS user_name", aliased.render(render))
    }

    @Test
    fun testMultipleAliasesFromSameExpression() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Create multiple different aliases from the same expression
        val base = Expr.column("id")
        val alias1 = base.alias("user_id")
        val alias2 = base.alias("member_id")
        val alias3 = base.alias("account_id")

        assertEquals("id AS user_id", alias1.render(render))
        assertEquals("id AS member_id", alias2.render(render))
        assertEquals("id AS account_id", alias3.render(render))
    }

    @Test
    fun testRenderConsistency() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that rendering the same expression multiple times produces consistent results
        val expr = Expr.functionCall("SUM", Expr.column("amount")).alias("total_amount")

        val first = expr.render(render)
        val second = expr.render(render)
        val third = expr.render(render)

        assertEquals(first, second)
        assertEquals(second, third)
    }
}

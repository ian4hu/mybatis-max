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
package com.github.ian4hu.mybatis.exposed

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.exposed.render.WrapperRender
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test for edge cases, boundary conditions, and error handling.
 */
class EdgeCaseTest : MybatisBootstrap {

    @Test
    fun testEmptyStringLiteral() {
        // Empty string should throw exception
        assertThrows<IllegalArgumentException> {
            Expr.literal("")
        }
    }

    @Test
    fun testInvalidColumnName() {
        // Column name with invalid characters
        assertThrows<IllegalArgumentException> {
            Expr.column("invalid-name")
        }

        assertThrows<IllegalArgumentException> {
            Expr.column("name with spaces")
        }

        assertThrows<IllegalArgumentException> {
            Expr.column("name;DROP")
        }
    }

    @Test
    fun testInvalidAliasName() {
        // Invalid alias name
        assertThrows<IllegalArgumentException> {
            Expr.column("name").alias("invalid-alias")
        }

        assertThrows<IllegalArgumentException> {
            Expr.column("name").alias("alias with space")
        }

        assertThrows<IllegalArgumentException> {
            Expr.column("name").alias("123alias") // starts with number
        }
    }

    @Test
    fun testInvalidFunctionName() {
        // Invalid function names
        assertThrows<IllegalArgumentException> {
            Expr.functionCall("invalid-func", Expr.constant(1))
        }

        assertThrows<IllegalArgumentException> {
            Expr.functionCall("func;DROP", Expr.constant(1))
        }

        assertThrows<IllegalArgumentException> {
            Expr.functionCall("", Expr.constant(1))
        }
    }

    @Test
    fun testQuotedStringLiterals() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test various quoted string formats
        assertEquals("''", Expr.literal("''").render(render))
        assertEquals("'hello'", Expr.literal("'hello'").render(render))
        assertEquals("\"world\"", Expr.literal("\"world\"").render(render))
        assertEquals("`column`", Expr.literal("`column`").render(render))

        // Test escaped quotes
        assertEquals("'can\\'t'", Expr.literal("'can\\'t'").render(render))
        assertEquals("'it''s'", Expr.literal("'it''s'").render(render))
    }

    @Test
    fun testInvalidQuotedLiterals() {
        // Unclosed quotes
        assertThrows<IllegalArgumentException> {
            Expr.literal("'unclosed")
        }

        // Mismatched quotes
        assertThrows<IllegalArgumentException> {
            Expr.literal("'mismatched\"")
        }

        // SQL injection attempts in quotes
        assertThrows<IllegalArgumentException> {
            Expr.literal("'\\\\'DROP TABLE;'")
        }
    }

    @Test
    fun testNumericLiterals() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Various numeric formats
        assertEquals("42", Expr.literal("42").render(render))
        assertEquals("-123", Expr.literal("-123").render(render))
        assertEquals("3.14", Expr.literal("3.14").render(render))
        assertEquals("-0.5", Expr.literal("-0.5").render(render))
        assertEquals("1.23E10", Expr.literal("1.23E10").render(render))
        assertEquals("1.5e-5", Expr.literal("1.5e-5").render(render))
        assertEquals("+123", Expr.literal("+123").render(render))
    }

    @Test
    fun testNullHandling() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Null constant
        assertEquals("NULL", Expr.constant(null).render(render))

        // Null variable
        val nullVar = Expr.variable(null)
        val result = nullVar.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result)
        assertNull(wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testSpecialSQLKeywords() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test SQL keywords as literals
        val keywords = listOf("NULL", "TRUE", "FALSE", "COUNT", "SUM", "MAX", "MIN", "AVG")
        keywords.forEach { keyword ->
            assertEquals(keyword, Expr.literal(keyword).render(render))
        }
    }

    @Test
    fun testQualifiedColumnNames() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Qualified column names (table.column)
        assertEquals("users.id", Expr.literal("users.id").render(render))
        assertEquals("schema.table.column", Expr.literal("schema.table.column").render(render))
        assertEquals("t.name", Expr.literal("t.name").render(render))
    }

    @Test
    fun testUnicodeInColumnNames() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Unicode characters in column names should work if they match identifier pattern
        // Most SQL databases don't support unicode in unquoted identifiers, but our pattern allows it
        assertEquals("column_name", Expr.column("column_name").render(render))
    }

    @Test
    fun testVariableWithMapping() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test all mapping methods
        val mapped = Expr.variable(100)
            .jdbcType("INTEGER")
            .javaType(Int::class.java)
            .mode("IN")
            .numericScale(2)

        val result = mapped.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1,jdbcType=INTEGER,javaType=int,mode=IN,numericScale=2}", result)

        // Test mode helpers
        val modeInVar = Expr.variable(200).modeIn()
        val inResult = modeInVar.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL2,mode=IN}", inResult)

        val modeOutVar = Expr.variable(300).modeOut()
        val outResult = modeOutVar.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL3,mode=OUT}", outResult)
    }

    @Test
    fun testVariableMappingChaining() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that mapping calls can be chained and overridden
        val var1 = Expr.variable("value")
            .jdbcType("VARCHAR")
            .jdbcType("TEXT") // Override previous

        val result = var1.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1,jdbcType=TEXT}", result)
    }

    @Test
    fun testFunctionWithNoArguments() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Function with no arguments
        val func = Expr.functionCall("NOW")
        assertEquals("NOW()", func.render(render))

        val func2 = Expr.functionCall("CURRENT_TIMESTAMP")
        assertEquals("CURRENT_TIMESTAMP()", func2.render(render))
    }

    @Test
    fun testFunctionWithMixedArguments() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Mix of different expression types as arguments
        val func = Expr.functionCall(
            "CONCAT",
            Expr.column("first_name"),
            Expr.constant(" "),
            Expr.column("last_name"),
            Expr.literal("'!'"),
            Expr.variable("suffix"),
        )

        val result = func.render(render)
        assertEquals(
            "CONCAT(first_name,#{ew.paramNameValuePairs.MPGENVAL1},last_name,'!',#{ew.paramNameValuePairs.MPGENVAL2})",
            result,
        )
    }

    @Test
    fun testNestedFunctionCalls() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Nested function calls
        val inner = Expr.functionCall("LOWER", Expr.column("name"))
        val outer = Expr.functionCall("TRIM", inner)
        val result = outer.render(render)
        assertEquals("TRIM(LOWER(name))", result)

        // Triple nesting
        val innermost = Expr.functionCall("SUBSTRING", Expr.column("text"), Expr.constant(1), Expr.constant(10))
        val middle = Expr.functionCall("UPPER", innermost)
        val outermost = Expr.functionCall("LTRIM", middle)
        val tripleResult = outermost.render(render)
        assertEquals("LTRIM(UPPER(SUBSTRING(text,1,10)))", tripleResult)
    }

    @Test
    fun testConstantWithSafeLiteralString() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Safe literal strings should render as literals, not variables
        assertEquals("NULL", Expr.constant("NULL").render(render))
        assertEquals("123", Expr.constant("123").render(render))
        assertEquals("3.14", Expr.constant("3.14").render(render))
        assertEquals("'hello'", Expr.constant("'hello'").render(render))

        // Unsafe strings should become variables
        val unsafeStr = Expr.constant("unsafe value")
        val result = unsafeStr.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result)
        assertEquals("unsafe value", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testDeeplyNestedLogicalExpressions() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Create deeply nested expression
        var expr: Condition = Expr.literal("A").asCondition().and(Expr.literal("B").asCondition())
        for (i in 3..10) {
            expr = expr.and(Expr.literal("L$i").asCondition())
        }

        val result = expr.render(render)
        // All should be flattened into single AND
        assertEquals("A AND B AND L3 AND L4 AND L5 AND L6 AND L7 AND L8 AND L9 AND L10", result)
    }

    @Test
    fun testMultipleNotOperations() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Double NOT should cancel out
        val doubleNot = Expr.literal("A").asCondition().not().not()
        assertEquals("A", doubleNot.render(render))

        // Triple NOT should be single NOT
        val tripleNot = Expr.literal("B").asCondition().not().not().not()
        assertEquals("NOT B", tripleNot.render(render))

        // Quadruple NOT should cancel out
        val quadNot = Expr.literal("C").asCondition().not().not().not().not()
        assertEquals("C", quadNot.render(render))
    }

    @Test
    fun testBooleanOperations() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test IS TRUE/FALSE/UNKNOWN
        assertEquals("col IS TRUE", Expr.column("col").isBool(true).render(render))
        assertEquals("col IS FALSE", Expr.column("col").isBool(false).render(render))
        assertEquals("col IS UNKNOWN", Expr.column("col").isBool(null).render(render))

        // Test IS NOT variants
        assertEquals("col IS NOT TRUE", Expr.column("col").isNotBool(true).render(render))
        assertEquals("col IS NOT FALSE", Expr.column("col").isNotBool(false).render(render))
        assertEquals("col IS NOT UNKNOWN", Expr.column("col").isNotBool(null).render(render))
    }

    @Test
    fun testComplexExpressionWithAllFeatures() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Combine all features in one complex expression
        val complex = Expr.functionCall(
            "IF",
            Expr.column("age").ge(Expr.constant(18))
                .and(Expr.column("verified").equalTo(Expr.constant(true)))
                .or(Expr.column("role").equalTo(Expr.literal("ADMIN"))),
            Expr.constant("ALLOWED"),
            Expr.constant("DENIED"),
        ).alias("access_status")

        val result = complex.render(render)
        val expected = "IF((age>=18 AND verified=true) OR role=ADMIN,ALLOWED,DENIED) AS access_status"
        assertEquals(expected, result)
    }

    @Test
    fun testExpressionImmutability() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Verify that operations create new instances
        val original = Expr.column("value")
        val withAlias = original.alias("val")
        val withCondition = original.equalTo(Expr.constant(10))

        // All should render differently
        assertEquals("value", original.render(render))
        assertEquals("value AS val", withAlias.render(render))
        assertEquals("value=10", withCondition.render(render))
    }
}

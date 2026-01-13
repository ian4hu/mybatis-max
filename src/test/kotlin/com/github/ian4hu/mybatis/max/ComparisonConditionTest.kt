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
import com.github.ian4hu.mybatis.max.render.WrapperRender
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive test for comparison operators and conditions.
 * Tests all comparison operators using Expr static methods and extension functions.
 */
class ComparisonConditionTest : MybatisBootstrap {

    @Test
    fun testEqualToOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test eq extension function
        val condition = Expr.column("age").eq(Expr.constant(18))
        val result = condition.render(render)
        assertEquals("age=18", result)

        // Test with variable
        val varCondition = Expr.column("name").eq(Expr.variable("John"))
        val varResult = varCondition.render(render)
        assertEquals("name=#{ew.paramNameValuePairs.MPGENVAL1}", varResult)
        assertEquals("John", wrapper.paramNameValuePairs["MPGENVAL1"])

        // Test with literal
        val litCondition = Expr.literal("status").eq(Expr.literal("ACTIVE"))
        val litResult = litCondition.render(render)
        assertEquals("status=ACTIVE", litResult)
    }

    @Test
    fun testNotEqualToOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("status").ne(Expr.constant(0))
        val result = condition.render(render)
        assertEquals("status<>0", result)

        // Test with null
        val nullCondition = Expr.column("deleted").ne(Expr.constant(null))
        val nullResult = nullCondition.render(render)
        assertEquals("deleted<>NULL", nullResult)
    }

    @Test
    fun testGreaterThanOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("price").gt(Expr.constant(100))
        val result = condition.render(render)
        assertEquals("price>100", result)

        // Test with decimal
        val decimalCondition = Expr.column("score").gt(Expr.constant(85.5))
        val decimalResult = decimalCondition.render(render)
        assertEquals("score>85.5", decimalResult)
    }

    @Test
    fun testGreaterOrEqualToOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("age").ge(Expr.constant(18))
        val result = condition.render(render)
        assertEquals("age>=18", result)

        // Test with long
        val longCondition = Expr.column("timestamp").ge(Expr.constant(1234567890L))
        val longResult = longCondition.render(render)
        assertEquals("timestamp>=1234567890", longResult)
    }

    @Test
    fun testLessThanOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("quantity").lt(Expr.constant(10))
        val result = condition.render(render)
        assertEquals("quantity<10", result)

        // Test with negative number
        val negCondition = Expr.column("balance").lt(Expr.constant(-100))
        val negResult = negCondition.render(render)
        assertEquals("balance<-100", negResult)
    }

    @Test
    fun testLessOrEqualToOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("retries").le(Expr.constant(3))
        val result = condition.render(render)
        assertEquals("retries<=3", result)

        // Test with float
        val floatCondition = Expr.column("temperature").le(Expr.constant(36.5f))
        val floatResult = floatCondition.render(render)
        assertEquals("temperature<=36.5", floatResult)
    }

    @Test
    fun testNullSafeEqualTo() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = Expr.column("optional_field").eqNullSafe(Expr.constant(null))
        val result = condition.render(render)
        assertEquals("optional_field<=>NULL", result)

        // Test with actual value
        val valueCondition = Expr.column("field").eqNullSafe(Expr.constant("value"))
        val valueResult = valueCondition.render(render)
        assertEquals("field<=>value", valueResult)
    }

    @Test
    fun testComparisonWithFunctionCalls() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test comparison with function call on left
        val leftFunc = Expr.functionCall("LENGTH", Expr.column("name")).gt(Expr.constant(10))
        val leftResult = leftFunc.render(render)
        assertEquals("LENGTH(name)>10", leftResult)

        // Test comparison with function call on right
        val rightFunc = Expr.column("created_at").ge(Expr.functionCall("NOW"))
        val rightResult = rightFunc.render(render)
        assertEquals("created_at>=NOW()", rightResult)

        // Test comparison between two functions
        val bothFunc = Expr.functionCall("COUNT", Expr.column("id"))
            .eq(Expr.functionCall("SUM", Expr.constant(1)))
        val bothResult = bothFunc.render(render)
        assertEquals("COUNT(id)=SUM(1)", bothResult)
    }

    @Test
    fun testComparisonWithComplexExpressions() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test comparison with aliased expressions (should work without alias in comparison)
        val col1 = Expr.column("a")
        val col2 = Expr.column("b")
        val condition = col1.eq(col2)
        val result = condition.render(render)
        assertEquals("a=b", result)
    }

    @Test
    fun testChainedComparisons() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test AND with multiple comparisons
        val andCondition = Expr.column("age").ge(Expr.constant(18))
            .and(Expr.column("age").le(Expr.constant(65)))
        val andResult = andCondition.render(render)
        assertEquals("age>=18 AND age<=65", andResult)

        // Test OR with multiple comparisons
        val orCondition = Expr.column("status").eq(Expr.literal("ACTIVE"))
            .or(Expr.column("status").eq(Expr.literal("PENDING")))
        val orResult = orCondition.render(render)
        assertEquals("status=ACTIVE OR status=PENDING", orResult)
    }

    @Test
    fun testComparisonWithAllPrimitiveTypes() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Byte
        val byteCondition = Expr.column("byte_field").eq(Expr.constant(42.toByte()))
        assertEquals("byte_field=42", byteCondition.render(render))

        // Short
        val shortCondition = Expr.column("short_field").eq(Expr.constant(1000.toShort()))
        assertEquals("short_field=1000", shortCondition.render(render))

        // Int
        val intCondition = Expr.column("int_field").eq(Expr.constant(123456))
        assertEquals("int_field=123456", intCondition.render(render))

        // Long
        val longCondition = Expr.column("long_field").eq(Expr.constant(9876543210L))
        assertEquals("long_field=9876543210", longCondition.render(render))

        // Float
        val floatCondition = Expr.column("float_field").eq(Expr.constant(3.14f))
        assertEquals("float_field=3.14", floatCondition.render(render))

        // Double
        val doubleCondition = Expr.column("double_field").eq(Expr.constant(2.718281828))
        assertEquals("double_field=2.718281828", doubleCondition.render(render))

        // Boolean
        val boolCondition = Expr.column("active").eq(Expr.constant(true))
        assertEquals("active=true", boolCondition.render(render))
    }

    @Test
    fun testComparisonConditionIsCondition() {
        // Test that comparison results implement Condition interface
        val condition = Expr.column("id").eq(Expr.constant(1))
        assertTrue(condition is Condition)
        assertNotNull(condition)

        // Test that condition can be used in logical operations
        val combined = condition.and(Expr.column("status").eq(Expr.literal("OK")))
        assertTrue(combined is Condition)
    }

    @Test
    fun testComparisonWithScientificNotation() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test with scientific notation literal
        val condition = Expr.column("big_number").gt(Expr.literal("1.5E10"))
        val result = condition.render(render)
        assertEquals("big_number>1.5E10", result)

        // Test with constant
        val constCondition = Expr.column("value").eq(Expr.constant("1.23E-5"))
        val constResult = constCondition.render(render)
        assertEquals("value=1.23E-5", constResult)
    }

    @Test
    fun testComparisonNegation() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test NOT on comparison
        val condition = Expr.column("age").lt(Expr.constant(18)).not()
        val result = condition.render(render)
        assertEquals("NOT age<18", result)

        // Test double negation
        val doubleNot = Expr.column("active").eq(Expr.constant(true)).not().not()
        val doubleResult = doubleNot.render(render)
        assertEquals("active=true", doubleResult)
    }

    @Test
    fun testComplexComparisonCombinations() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test complex condition: (age >= 18 AND age <= 65) OR status = 'VIP'
        val ageRange = Expr.column("age").ge(Expr.constant(18))
            .and(Expr.column("age").le(Expr.constant(65)))
        val vipStatus = Expr.column("status").eq(Expr.literal("VIP"))
        val complex = ageRange.or(vipStatus)
        val result = complex.render(render)
        assertEquals("(age>=18 AND age<=65) OR status=VIP", result)
    }

    @Test
    fun testComparisonWithVariousColumns() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test with snake_case column
        val snakeCase = Expr.column("user_name").eq(Expr.constant("admin"))
        assertEquals("user_name=admin", snakeCase.render(render))

        // Test with camelCase column (should be kept as is)
        val camelCase = Expr.column("userId").eq(Expr.constant(123))
        assertEquals("userId=123", camelCase.render(render))

        // Test with uppercase column
        val uppercase = Expr.column("ID").ne(Expr.constant(0))
        assertEquals("ID<>0", uppercase.render(render))
    }
}

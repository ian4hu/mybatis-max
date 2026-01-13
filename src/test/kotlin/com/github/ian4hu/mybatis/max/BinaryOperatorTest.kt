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
 * Test for bitwise operators and additional operator coverage.
 */
class BinaryOperatorTest : MybatisBootstrap {

    @Test
    fun testBitwiseAndOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test bitwise AND using BinaryOp directly
        val condition = BinaryOp.BIT_AND.of(Expr.column("flags"), Expr.constant(1))
        val result = condition.render(render)
        assertEquals("flags & 1", result)

        // Test with multiple operands
        val multiCondition = BinaryOp.BIT_AND.of(
            Expr.column("a"),
            Expr.constant(0xFF),
            Expr.constant(0x0F),
        )
        val multiResult = multiCondition.render(render)
        assertEquals("a & 255 & 15", multiResult)
    }

    @Test
    fun testBitwiseOrOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = BinaryOp.BIT_OR.of(Expr.column("permissions"), Expr.constant(4))
        val result = condition.render(render)
        assertEquals("permissions | 4", result)

        // Test with literals
        val litCondition = BinaryOp.BIT_OR.of(Expr.literal("READ"), Expr.literal("WRITE"))
        val litResult = litCondition.render(render)
        assertEquals("READ | WRITE", litResult)
    }

    @Test
    fun testBitwiseXorOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        val condition = BinaryOp.BIT_XOR.of(Expr.column("mask"), Expr.constant(0xAA))
        val result = condition.render(render)
        assertEquals("mask ^ 170", result)

        // Test chaining
        val chainCondition = BinaryOp.BIT_XOR.of(
            Expr.constant(1),
            Expr.constant(2),
            Expr.constant(3),
        )
        val chainResult = chainCondition.render(render)
        assertEquals("1 ^ 2 ^ 3", chainResult)
    }

    @Test
    fun testLogicalXorOperator() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test XOR with static method
        val condition = Expr.xor(Expr.literal("A"), Expr.literal("B"))
        val result = condition.render(render)
        assertEquals("A XOR B", result)

        // Test XOR with instance method
        val instanceCondition = Expr.literal("X").xor(Expr.literal("Y"))
        val instanceResult = instanceCondition.render(render)
        assertEquals("X XOR Y", instanceResult)

        // Test multiple XOR
        val multiCondition = Expr.xor(
            Expr.literal("P"),
            Expr.literal("Q"),
            Expr.literal("R"),
        )
        val multiResult = multiCondition.render(render)
        assertEquals("P XOR Q XOR R", multiResult)
    }

    @Test
    fun testXorFlattening() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that nested XOR expressions are flattened
        val innerXor = Expr.literal("A").xor(Expr.literal("B"))
        val outerXor = innerXor.xor(Expr.literal("C"))
        val result = outerXor.render(render)
        assertEquals("A XOR B XOR C", result)
    }

    @Test
    fun testXorWithDuplicates() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that duplicate expressions are removed
        val litA = Expr.literal("A")
        val condition = Expr.xor(litA, Expr.literal("B"), litA, Expr.literal("C"))
        val result = condition.render(render)
        // Duplicates should be removed, so only A, B, C remain
        assertEquals("A XOR B XOR C", result)
    }

    @Test
    fun testMixedBitwiseOperations() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test combining different bitwise operations
        val andOp = BinaryOp.BIT_AND.of(Expr.column("flags"), Expr.constant(0x0F))
        val orOp = BinaryOp.BIT_OR.of(andOp, Expr.constant(0x10))
        val result = orOp.render(render)
        assertEquals("(flags & 15) | 16", result)
    }

    @Test
    fun testBitwiseWithLogicalOperators() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test bitwise AND in logical AND
        val bitAnd = BinaryOp.BIT_AND.of(Expr.column("status"), Expr.constant(1))
        val logicalAnd = bitAnd.and(Expr.column("active").eq(Expr.constant(true)))
        val result = logicalAnd.render(render)
        assertEquals("(status & 1) AND active=true", result)
    }

    @Test
    fun testAllBinaryOperators() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test all binary operators
        val operators = mapOf(
            BinaryOp.AND to "A AND B",
            BinaryOp.OR to "A OR B",
            BinaryOp.XOR to "A XOR B",
            BinaryOp.BIT_AND to "A & B",
            BinaryOp.BIT_OR to "A | B",
            BinaryOp.BIT_XOR to "A ^ B",
        )

        operators.forEach { (op, expected) ->
            val condition = op.of(Expr.literal("A"), Expr.literal("B"))
            val result = condition.render(render)
            assertEquals(expected, result, "Operator ${op.name} failed")
        }
    }

    @Test
    fun testBinaryOperatorWithCompositeExpressions() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test that composite expressions are wrapped in parentheses
        val inner = Expr.literal("A").and(Expr.literal("B"))
        val outer = BinaryOp.BIT_AND.of(inner, Expr.constant(1))
        val result = outer.render(render)
        assertEquals("(A AND B) & 1", result)
    }

    @Test
    fun testBinaryOperatorResultIsCondition() {
        // Verify all binary operators return Condition
        val a = Expr.literal("A")
        val b = Expr.literal("B")

        assertTrue(BinaryOp.AND.of(a, b) is Condition)
        assertTrue(BinaryOp.OR.of(a, b) is Condition)
        assertTrue(BinaryOp.XOR.of(a, b) is Condition)
        assertTrue(BinaryOp.BIT_AND.of(a, b) is Condition)
        assertTrue(BinaryOp.BIT_OR.of(a, b) is Condition)
        assertTrue(BinaryOp.BIT_XOR.of(a, b) is Condition)
    }

    @Test
    fun testBinaryOperatorWithVariables() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test bitwise operators with variables
        val var1 = Expr.variable(255)
        val var2 = Expr.variable(15)
        val condition = BinaryOp.BIT_AND.of(var1, var2)
        val result = condition.render(render)
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1} & #{ew.paramNameValuePairs.MPGENVAL2}", result)
        assertEquals(255, wrapper.paramNameValuePairs["MPGENVAL1"])
        assertEquals(15, wrapper.paramNameValuePairs["MPGENVAL2"])
    }

    @Test
    fun testBinaryOperatorWithFunctions() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test bitwise operators with function calls
        val func = Expr.functionCall("GET_FLAG", Expr.column("id"))
        val condition = BinaryOp.BIT_AND.of(func, Expr.constant(1))
        val result = condition.render(render)
        assertEquals("GET_FLAG(id) & 1", result)
    }

    @Test
    fun testSingleElementOptimization() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // When flattening results in single element, should return it directly
        // This is hard to test directly, but we can verify the behavior through nested operations
        val single = Expr.literal("ONLY")
        val condition = BinaryOp.AND.of(single, single) // Duplicates removed, only one remains
        val result = condition.render(render)
        // With only one element, it should not have AND operator
        assertEquals("ONLY", result)
    }

    @Test
    fun testComplexNestedBinaryOperations() {
        val wrapper = Wrappers.query<Any>()
        val render = WrapperRender(wrapper)

        // Test deeply nested operations
        val a = Expr.literal("A")
        val b = Expr.literal("B")
        val c = Expr.literal("C")
        val d = Expr.literal("D")

        // ((A AND B) OR (C AND D)) XOR E
        val ab = a.and(b)
        val cd = c.and(d)
        val or = ab.or(cd)
        val xor = or.xor(Expr.literal("E"))

        val result = xor.render(render)
        assertEquals("((A AND B) OR (C AND D)) XOR E", result)
    }
}

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
package com.github.ian4hu.mybatis.exposed.dsl

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.MybatisBootstrap
import com.github.ian4hu.mybatis.exposed.entity.SampleDBO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

/**
 * Unit tests for infix comparison operators in WhereDSL.
 * Tests cover all existing comparison functions with various parameter types and combinations.
 *
 * @author ian
 * @date 2026/01/25
 */
class InfixOperatorTest : MybatisBootstrap {

    // ============ eq (equality) operator tests ============

    @Test
    fun testInfixEqWithString() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"name" eq "Alice"
            }
        }
        assertEquals("(name=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals("Alice", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixEqWithKProperty() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::id eq 100
            }
        }
        assertEquals("(id=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(100, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixEqWithNull() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::metadata eq null
            }
        }
        assertEquals("(metadata=NULL)", wrapper.sqlSegment)
    }

    @Test
    fun testInfixEqWithExpr() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::id eq +SampleDBO::outBizId
            }
        }
        assertEquals("(id=out_biz_id)", wrapper.sqlSegment)
    }

    // ============ ne (not equal) operator tests ============

    @Test
    fun testInfixNeWithString() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"status" ne "DELETED"
            }
        }
        assertEquals("(status<>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals("DELETED", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixNeWithNumber() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::id ne 0
            }
        }
        assertEquals("(id<>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(0, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixNeWithNull() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::buffer ne null
            }
        }
        assertEquals("(buffer<>NULL)", wrapper.sqlSegment)
    }

    // ============ gt (greater than) operator tests ============

    @Test
    fun testInfixGtWithInteger() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"age" gt 18
            }
        }
        assertEquals("(age>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(18, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixGtWithLong() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::buffSize gt 1024L
            }
        }
        assertEquals("(buff_size>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(1024L, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixGtWithDouble() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"price" gt 99.99
            }
        }
        assertEquals("(price>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(99.99, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    // ============ ge (greater or equal) operator tests ============

    @Test
    fun testInfixGeWithInteger() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"age" ge 18
            }
        }
        assertEquals("(age>=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(18, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixGeWithKProperty() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::buffSize ge 0
            }
        }
        assertEquals("(buff_size>=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(0, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    // ============ lt (less than) operator tests ============

    @Test
    fun testInfixLtWithInteger() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"age" lt 60
            }
        }
        assertEquals("(age<#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(60, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixLtWithDouble() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"price" lt 100.0
            }
        }
        assertEquals("(price<#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(100.0, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    // ============ le (less or equal) operator tests ============

    @Test
    fun testInfixLeWithInteger() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"count" le 100
            }
        }
        assertEquals("(count<=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(100, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixLeWithKProperty() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::buffSize le 1048576
            }
        }
        assertEquals("(buff_size<=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(1048576, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    // ============ eqNullSafe (null-safe equality) operator tests ============

    @Test
    fun testInfixEqNullSafeWithValue() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"value" eqNullSafe "test"
            }
        }
        assertEquals("(value<=>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals("test", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixEqNullSafeWithNull() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::metadata eqNullSafe null
            }
        }
        assertEquals("(metadata<=>NULL)", wrapper.sqlSegment)
    }

    // ============ Combination tests with logical operators ============

    @Test
    fun testInfixWithAndOperator() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                (+"age" gt 18) and (+"age" lt 60)
            }
        }
        assertEquals(
            "((age>#{ew.paramNameValuePairs.MPGENVAL1} AND age<#{ew.paramNameValuePairs.MPGENVAL2}))",
            wrapper.sqlSegment,
        )
        assertEquals(18, wrapper.paramNameValuePairs["MPGENVAL1"])
        assertEquals(60, wrapper.paramNameValuePairs["MPGENVAL2"])
    }

    @Test
    fun testInfixWithOrOperator() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                (+"status" eq "ACTIVE") or (+"status" eq "PENDING")
            }
        }
        assertEquals(
            "((status=#{ew.paramNameValuePairs.MPGENVAL1} OR status=#{ew.paramNameValuePairs.MPGENVAL2}))",
            wrapper.sqlSegment,
        )
        assertEquals("ACTIVE", wrapper.paramNameValuePairs["MPGENVAL1"])
        assertEquals("PENDING", wrapper.paramNameValuePairs["MPGENVAL2"])
    }

    @Test
    fun testComplexNestedConditions() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                ((+SampleDBO::id eq +SampleDBO::outBizId) and (+SampleDBO::type eq "VIP")) or
                    (+SampleDBO::mediaType eq "ACTIVE")
            }
        }
        assertTrue(wrapper.sqlSegment.contains("id=out_biz_id"))
        assertTrue(wrapper.sqlSegment.contains("type="))
        assertTrue(wrapper.sqlSegment.contains("media_type="))
    }

    @Test
    fun testMultipleAndConditions() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                (+SampleDBO::id gt 0) and
                    (+SampleDBO::type eq "IMAGE") and
                    (+SampleDBO::buffSize le 1048576)
            }
        }
        assertTrue(wrapper.sqlSegment.contains("id>"))
        assertTrue(wrapper.sqlSegment.contains("type="))
        assertTrue(wrapper.sqlSegment.contains("buff_size<="))
    }

    // ============ Edge cases and boundary tests ============

    @Test
    fun testInfixWithZeroValue() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"value" eq 0
            }
        }
        assertEquals("(value=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(0, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixWithNegativeValue() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"temperature" lt -10
            }
        }
        assertEquals("(temperature<#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(-10, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixWithEmptyString() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"name" ne ""
            }
        }
        assertEquals("(name<>#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals("", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixWithBoolean() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +"active" eq true
            }
        }
        assertEquals("(active=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
        assertEquals(true, wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    @Test
    fun testInfixWithLiteralExpr() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::id eq Expr.literal("100")
            }
        }
        assertEquals("(id=100)", wrapper.sqlSegment)
    }

    @Test
    fun testInfixWithVariableExpr() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                +SampleDBO::id eq Expr.variable("testValue")
            }
        }
        assertTrue(wrapper.sqlSegment.contains("id="))
        assertEquals("testValue", wrapper.paramNameValuePairs["MPGENVAL1"])
    }

    // ============ Backward compatibility tests ============

    @Test
    fun testOldStyleEqualToStillWorks() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                (+"A").equalTo(Expr.variable("1"))
            }
        }
        assertEquals("(A=#{ew.paramNameValuePairs.MPGENVAL1})", wrapper.sqlSegment)
    }

    @Test
    fun testMixedOldAndNewStyle() {
        val wrapper = sql(Wrappers.query<Any>()) {
            where {
                ((+"name" eq "Alice") and (+"age").equalTo(Expr.variable(18)))
            }
        }
        assertTrue(wrapper.sqlSegment.contains("name="))
        assertTrue(wrapper.sqlSegment.contains("age="))
    }
}

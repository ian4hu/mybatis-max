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
package com.github.ian4hu.mybatis.max.dsl

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.max.Expr
import com.github.ian4hu.mybatis.max.MybatisBootstrap
import com.github.ian4hu.mybatis.max.entity.SampleDBO
import com.github.ian4hu.mybatis.max.eq
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * @author ian
 * @date 2026/01/14
 */
class SqlDSLTest : MybatisBootstrap {

    @Test
    fun testQuery() {
        val wrapper = Wrappers.query<Any>()
        sql(wrapper) {
            select {
                +Expr.column("id")
                +Expr.column("name") AS "A"
                +SampleDBO::sha256
                +SampleDBO::sha256 AS "_sha256"
                +Expr.functionCall("now") AS "time"
                -SampleDBO::id
            }

            where {
                Expr.column("name") eq Expr.literal("A") and
                    (Expr.column("id").isNotNull()) or
                    (Expr.column("id").isNotNull()) or
                    (Expr.column("name") eq "1")
            }
        }

        assertEquals("id,name AS A,sha256,sha256 AS _sha256,now() AS time", wrapper.sqlSelect)
        assertEquals("(((name=A AND id IS NOT NULL) OR id IS NOT NULL OR name=#{ew.paramNameValuePairs.MPGENVAL1}))", wrapper.sqlSegment)

        assertEquals(
            "buff_size,buffer,gmt_create,gmt_modified,id,media_type,metadata,out_biz_id,sha256,type",
            sql {
                select {
                    // include all columns
                    +SampleDBO::class
                }
            }.renderTo(Wrappers.query<Any>()).sqlSelect,
        )

        assertEquals(
            "buff_size,gmt_create,gmt_modified,id,media_type,out_biz_id,sha256,type",
            sql {
                select {
                    // include all columns
                    +SampleDBO::class
                    // exclude metadata and buffer
                    -SampleDBO::metadata
                    -SampleDBO::buffer
                }
            }.renderTo(Wrappers.query<Any>()).sqlSelect,
        )
    }

    @Test
    fun testAliasTwice() {
        assertFailsWith<IllegalArgumentException> {
            sql {
                select {
                    val tracker = +SampleDBO::id
                    tracker AS "id"
                    tracker AS "id"
                }
            }
        }
    }

    @Test
    fun testWhereTwice() {
        assertFailsWith<IllegalArgumentException> {
            sql {
                where {
                    Expr.column("id").isNotNull()
                    Expr.column("id").isNotNull()
                }
                where { Expr.column("name").isNotNull() }
            }
        }
    }

    @Test
    fun testWhere() {
        assertEquals("", sql(Wrappers.query<Any>()) {}.sqlSegment)
        assertEquals(
            "(A=#{ew.paramNameValuePairs.MPGENVAL1})",
            sql(Wrappers.query<Any>()) {
                where {
                    +"A" eq "1"
                }
            }.sqlSegment,
        )

        assertEquals(
            "(id=#{ew.paramNameValuePairs.MPGENVAL1})",
            sql(Wrappers.query<Any>()) {
                where {
                    +SampleDBO::id eq "1"
                }
            }.sqlSegment,
        )
    }

    @Test
    fun testNestedWhereCondition() {
        assertEquals(
            "(((id=out_biz_id AND id=A) OR id=B))",
            sql(Wrappers.query<Any>()) {
                where {
                    (SampleDBO::id eq SampleDBO::outBizId) and
                        (SampleDBO::id eq Expr.literal("A")) or
                        (SampleDBO::id eq Expr.literal("B"))
                }
            }.sqlSegment,
        )
    }

    @Test
    fun testNestedWhereConditionOr() {
        assertEquals(
            "((id=out_biz_id OR id=#{ew.paramNameValuePairs.MPGENVAL1}))",
            sql(Wrappers.query<Any>()) {
                where {
                    (SampleDBO::id eq SampleDBO::outBizId) or
                        (SampleDBO::id eq "1")
                }
            }.sqlSegment,
        )
    }
}

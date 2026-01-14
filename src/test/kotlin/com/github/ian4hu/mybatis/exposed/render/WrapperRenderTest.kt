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
package com.github.ian4hu.mybatis.exposed.render

import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.exposed.Condition
import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.MybatisBootstrap
import com.github.ian4hu.mybatis.exposed.eq
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author ian
 * @date 2026/01/12
 */
class WrapperRenderTest : MybatisBootstrap {

    @Test
    fun testSelect() {
        val wrapper = Wrappers.query<Any>()
        wrapper.addSelect(Expr.column("id").alias("uid") to true)
        assertEquals("id AS uid", wrapper.sqlSelect)

        wrapper.addSelect(Expr.column("name").alias("username") to false)
        assertEquals("id AS uid", wrapper.sqlSelect)

        wrapper.clearSelect(false)
        assertEquals("id AS uid", wrapper.sqlSelect)

        wrapper.clearSelect()

        wrapper.addSelect(Expr.column("age").alias("years_old"))
        assertEquals("age AS years_old", wrapper.sqlSelect)

        wrapper.addSelect(Expr.functionCall("concat", Expr.column("id"), Expr.constant("-"), Expr.column("name")))
        assertEquals("age AS years_old,concat(id,#{ew.paramNameValuePairs.MPGENVAL1},name)", wrapper.sqlSelect)
    }

    @Test
    fun testCondidtion() {
        val wrapper = Wrappers.query<Any>()
        assertEquals("", wrapper.sqlSegment)
        wrapper.addCondition(Condition.and(Condition.literal("A"), Condition.literal("B")))
        assertEquals("((A AND B))", wrapper.sqlSegment)

        wrapper.or(Condition.literal("C").or(Condition.literal("D")))
        assertEquals("((A AND B) OR (C OR D))", wrapper.sqlSegment)

        wrapper.clearCondition(false)

        assertEquals("((A AND B) OR (C OR D))", wrapper.sqlSegment)

        wrapper.clearCondition()

        wrapper.and(Condition.literal("E").or(Condition.literal("F")))
        assertEquals("((E OR F))", wrapper.sqlSegment)

        wrapper.and(Expr.column("id").eq(Expr.literal("12")).and(Expr.column("username").eq(Expr.functionCall("current_timestamp"))))
        assertEquals("((E OR F) AND (id=12 AND username=current_timestamp()))", wrapper.sqlSegment)
    }
}

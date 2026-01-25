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
package com.github.ian4hu.mybatis.exposed.mapper

import com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.github.ian4hu.mybatis.exposed.Expr
import com.github.ian4hu.mybatis.exposed.MybatisBootstrap
import com.github.ian4hu.mybatis.exposed.entity.SampleDBO
import com.github.ian4hu.mybatis.exposed.render.addCondition
import com.github.ian4hu.mybatis.exposed.render.addSelect
import com.github.ian4hu.mybatis.exposed.render.and
import com.github.ian4hu.mybatis.exposed.render.clearCondition
import com.github.ian4hu.mybatis.exposed.render.or
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author ian
 * @date 2026/01/14
 */
class SampleTest : MybatisBootstrap {

    val sampleMapper: SampleMapper by lazy {
        val sqlSessionFactory = SqlSessionFactoryBuilder().build(MybatisBootstrap.CONFIGURATION.value)
        val mapperProxyFactory = MybatisMapperProxyFactory(SampleMapper::class.java)
        mapperProxyFactory.newInstance(sqlSessionFactory.openSession())
    }

    fun concat(vararg args: Any?) = Expr.functionCall("concat", *args)

    @Test
    fun testQuery() {
        val entity = SampleDBO()
        entity.buffer = "Hello World".toByteArray()
        entity.buffSize = entity.buffer?.size?.toLong()
        entity.sha256 = "xxx"
        entity.metadata = "{}"
        entity.type = "file"
        entity.outBizId = "1"
        entity.gmtModified = Date()
        entity.gmtCreate = Date()
        val cnt = sampleMapper.insert(entity)
        assertEquals(1, cnt)

        val queryWrapper = Wrappers.query<SampleDBO>()
        queryWrapper.addSelect(
            Expr.kotlinProperty(SampleDBO::id),
            Expr.kotlinProperty(SampleDBO::sha256),
            Expr.kotlinProperty(SampleDBO::type),
            concat(Expr.kotlinProperty(SampleDBO::sha256), ':', Expr.kotlinProperty(SampleDBO::type)).alias("out_biz_id"),
        )
            .addCondition(Expr.kotlinProperty(SampleDBO::type).equalTo(Expr.variable("file")))
            .and(Expr.kotlinProperty(SampleDBO::type).isNotNull())
            .and(concat(Expr.kotlinProperty(SampleDBO::sha256), ':', Expr.kotlinProperty(SampleDBO::type)).equalTo(Expr.variable("${entity.sha256}:${entity.type}")))
            .or(Expr.kotlinProperty(SampleDBO::type).isNull())
        val savedEntity = sampleMapper.selectOne(queryWrapper)
        assertEquals(entity.sha256, savedEntity.sha256)
        assertEquals("${entity.sha256}:${entity.type}", savedEntity.outBizId)
        assertEquals("(type=#{ew.paramNameValuePairs.MPGENVAL2} AND type IS NOT NULL AND concat(sha256,#{ew.paramNameValuePairs.MPGENVAL3},type)=#{ew.paramNameValuePairs.MPGENVAL4} OR type IS NULL)", queryWrapper.sqlSegment)
        assertEquals("id,sha256,type,concat(sha256,#{ew.paramNameValuePairs.MPGENVAL1},type) AS out_biz_id", queryWrapper.sqlSelect)

        queryWrapper.clearCondition()
            .or(Expr.kotlinProperty(SampleDBO::type).isNotNull().and(Expr.kotlinProperty(SampleDBO::outBizId).equalTo(Expr.variable("file"))))
            .or(Expr.kotlinProperty(SampleDBO::type).isNull().and(Expr.kotlinProperty(SampleDBO::outBizId).equalTo(Expr.variable("file"))))
        assertEquals("((type IS NOT NULL AND out_biz_id=#{ew.paramNameValuePairs.MPGENVAL5}) OR (type IS NULL AND out_biz_id=#{ew.paramNameValuePairs.MPGENVAL6}))", queryWrapper.sqlSegment)
    }
}

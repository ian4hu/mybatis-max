package com.github.ian4hu.mybatis.max

import com.baomidou.mybatisplus.core.conditions.Helper
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.query.Query
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertEquals

/**
 * @author ian
 * @date 2026/01/07
 */
internal class HelperTest : MybatisBootstrap {

    @Test
    fun testColumn() {

        for (wrapper in arrayOf(
            Wrappers.query(BlockStorageDBO::class.java),
            Wrappers.lambdaQuery(BlockStorageDBO::class.java),
            KtQueryWrapper(BlockStorageDBO())
        )) {
            val wrapColumn = Helper.wrapColumn(wrapper, "name")
            assertEquals("name", wrapColumn);

            val wrapLambda = Helper.wrapLambda(wrapper, JavaHelperTest.metadata())
            assertEquals("metadata", wrapLambda)

            val wrapProp = Helper.wrapProperty(wrapper, BlockStorageDBO::buffer)
            assertEquals("buffer", wrapProp)

            val param = Helper.wrapParam(wrapper, "A", null)
            val paramNameValuePairs = wrapper.getParamNameValuePairs()
            Assertions.assertFalse(paramNameValuePairs.isEmpty())
            assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", param)
            assertEquals("A", paramNameValuePairs.get("MPGENVAL1"))
        }

    }

    @Test
    fun testSqlSelect() {

        val wrappers = mutableSetOf<Query<*, *, *>>().apply {
            val ktQueryWrapper = KtQueryWrapper(BlockStorageDBO::class.java)
            ktQueryWrapper.select(BlockStorageDBO::metadata)
            add(ktQueryWrapper)

            val lambdaQueryWrapper = LambdaQueryWrapper<BlockStorageDBO>()
            lambdaQueryWrapper.select(JavaHelperTest.metadata())
            add(lambdaQueryWrapper)

            val queryWrapper = QueryWrapper<BlockStorageDBO>()
            queryWrapper.select("metadata")
            add(queryWrapper)
        }

        for (wrapper in wrappers) {
            val sqlSelect = Helper.getSqlSelect(wrapper)
            assertNotNull(sqlSelect)
            assertEquals("metadata", sqlSelect.stringValue)
        }
    }
}
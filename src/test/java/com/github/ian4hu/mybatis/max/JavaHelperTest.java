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
package com.github.ian4hu.mybatis.max;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baomidou.mybatisplus.core.conditions.Helper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.ian4hu.mybatis.max.entity.SampleDBO;

import org.junit.jupiter.api.Test;

/**
 * @author ian
 * @date 2026/01/07
 */
public class JavaHelperTest implements MybatisBootstrap {

	@Test
	public void testWrapColumn() {
		String column = Helper.wrapLambda(Wrappers.lambdaQuery(SampleDBO.class), SampleDBO::getBuffer);
		assertEquals("buffer", column);
	}

	public static SFunction<SampleDBO, String> metadata() {
		return SampleDBO::getMetadata;
	}
}

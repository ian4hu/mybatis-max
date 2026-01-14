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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.ian4hu.mybatis.max.entity.SampleDBO;
import com.github.ian4hu.mybatis.max.render.WrapperRender;

import org.junit.jupiter.api.Test;

import kotlin.Pair;

public class JavaExprTest implements MybatisBootstrap {
	@Test
	public void testPrimitiveConstant() {
		Map<Object, String> map = new HashMap<>();
		map.put(1, "1");
		map.put(2L, "2");
		map.put((byte) 3, "3");
		map.put(1.1F, "1.1");
		map.put(1.2D, "1.2");
		map.put(true, "true");
		map.put(null, "NULL");

		for (Map.Entry<Object, String> entry : map.entrySet()) {
			Object value = entry.getKey();
			String expr = entry.getValue();
			String result = Expr.constant(value).render(new WrapperRender(Wrappers.query()));
			assertEquals(expr, result);
		}
	}

	@Test
	public void testNonePrimitiveConstant() {
		QueryWrapper<Object> wrapper = Wrappers.query();
		WrapperRender render = new WrapperRender(wrapper);
		String result = Expr.constant("1x1").render(render);
		assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result);
		assertEquals("1x1", wrapper.getParamNameValuePairs().get("MPGENVAL1"));

		String NULL = Expr.constant("NULL").render(render);
		assertEquals("NULL", NULL);

		String num = Expr.constant("1.23E10").render(render);
		assertEquals("1.23E10", num);

		String safeLiteralString = Expr.constant("'hello world'").render(render);
		assertEquals("'hello world'", safeLiteralString);

		Pair<String, String> param = new Pair<>("A", "B");
		String objParam = Expr.constant(param).render(render);
		assertEquals("#{ew.paramNameValuePairs.MPGENVAL2}", objParam);
		assertEquals(param, wrapper.getParamNameValuePairs().get("MPGENVAL2"));
	}

	@Test
	public void testLambda() {
		List<Supplier<AbstractWrapper<?, ?, ?>>> wrappers = Arrays.asList(Wrappers::query,
			() -> Wrappers.lambdaQuery(SampleDBO.class));
		for (Supplier<AbstractWrapper<?, ?, ?>> wrapper : wrappers) {
			String result = Expr.lambda(SampleDBO::getOutBizId).render(new WrapperRender(wrapper.get()));
			assertEquals("out_biz_id", result);
		}
	}
}

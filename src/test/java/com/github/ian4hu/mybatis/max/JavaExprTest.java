package com.github.ian4hu.mybatis.max;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaExprTest implements MybatisBootstrap {
    @Test
    public void testPrimitiveConstant() {
        Map<Object, String> map = new HashMap<>();
        map.put(1, "1");
        map.put(2L, "2");
        map.put((byte)3, "3");
        map.put(1.1F, "1.1");
        map.put(1.2D, "1.2");
        map.put(true, "true");
        map.put(null, "NULL");

        for (Map.Entry<Object, String> entry : map.entrySet()) {
            Object value = entry.getKey();
            String expr = entry.getValue();
            String result = Expr.constant(value).render(Wrappers.query());
            assertEquals(expr, result);
        }
    }

    @Test
    public void testNonePrimitiveConstant() {
        QueryWrapper<Object> wrapper = Wrappers.query();
        String result = Expr.constant("str").render(wrapper);
        assertEquals("#{ew.paramNameValuePairs.MPGENVAL1}", result);
        assertEquals("str", wrapper.getParamNameValuePairs().get("MPGENVAL1"));
    }

    @Test
    public void testLambda() {
        List<Supplier<AbstractWrapper<?,?,?>>> wrappers = Arrays.asList(Wrappers::query, () -> Wrappers.lambdaQuery(BlockStorageDBO.class));
        for (Supplier<AbstractWrapper<?,?,?>> wrapper : wrappers) {
            String result = Expr.lambda(BlockStorageDBO::getOutBizId).render(wrapper.get());
            assertEquals("out_biz_id", result);
        }
    }
}

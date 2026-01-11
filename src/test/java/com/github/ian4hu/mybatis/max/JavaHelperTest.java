package com.github.ian4hu.mybatis.max;

import com.baomidou.mybatisplus.core.conditions.Helper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.ian4hu.mybatis.max.entity.BlockStorageDBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ian
 * @date 2026/01/07
 */
public class JavaHelperTest implements MybatisBootstrap {

    @Test
    public void testWrapColumn() {
        String column = Helper.wrapLambda(Wrappers.lambdaQuery(BlockStorageDBO.class), BlockStorageDBO::getBuffer);
        assertEquals("buffer", column);
    }

    public static SFunction<BlockStorageDBO, String> metadata() {
        return BlockStorageDBO::getMetadata;
    }
}

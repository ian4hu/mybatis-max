package com.github.ian4hu.mybatis.max;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.github.ian4hu.mybatis.max.mapper.BlockStorageMapper;
import kotlin.Lazy;
import kotlin.LazyKt;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface MybatisBootstrap {

    Lazy<MybatisConfiguration> CONFIGURATION = LazyKt.lazy(() -> {
        MybatisConfiguration config = new MybatisConfiguration();
        config.addMapper(BlockStorageMapper.class);
        return config;
    });

    @BeforeAll
    static void setUpMyBatis() {
        assertNotNull(CONFIGURATION.getValue());
    }
}

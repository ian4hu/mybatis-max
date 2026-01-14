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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.github.ian4hu.mybatis.max.mapper.SampleMapper;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;

import kotlin.Lazy;
import kotlin.LazyKt;

public interface MybatisBootstrap {

	Lazy<MybatisConfiguration> CONFIGURATION = LazyKt.lazy(() -> {
		MybatisConfiguration config = new MybatisConfiguration();
		DataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
		Properties props = new Properties();
		props.setProperty("driver", "org.h2.Driver");
		props.setProperty("url", "jdbc:h2:mem:testdb;MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
		dataSourceFactory.setProperties(props);
		JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();
		DataSource dataSource = dataSourceFactory.getDataSource();
		config.setEnvironment(new Environment("test", transactionFactory, dataSource));
		config.addMapper(SampleMapper.class);

		try (Connection con = dataSource.getConnection()) {
			con.createStatement().execute("create table if not exists sample\n" + "(\n"
					+ "    id           bigint auto_increment,\n"
					+ "    gmt_create   timestamp             default current_timestamp,\n"
					+ "    gmt_modified timestamp on update current_timestamp,\n" + "    out_biz_id   varchar(128),\n"
					+ "    type         varchar(32) ,\n" + "    sha256       varchar(256),\n"
					+ "    media_type   varchar(64) default 'application/octet-stream',\n" + "    metadata    text,\n"
					+ "    buff_size    bigint,\n" + "    buffer       longblob,\n" + "    constraint sample_pk\n"
					+ "        primary key (id),\n" + "    index sample_out_biz_id_index(out_biz_id),\n"
					+ "    index sampl" + "e_type_index(type)\n" + ");");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return config;
	});

	@BeforeAll
	static void setUpMyBatis() {
		assertNotNull(CONFIGURATION.getValue());
	}
}

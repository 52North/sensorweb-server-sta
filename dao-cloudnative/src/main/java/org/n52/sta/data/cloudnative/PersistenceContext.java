/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.data.cloudnative;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.*;
import org.jooq.impl.*;

import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import javax.sql.DataSource;
import java.sql.SQLException;


@Configuration
@EnableConfigurationProperties
public class PersistenceContext {

    @Value("${spring.athena.user}")
    private String user;

    @Value("${spring.athena.password}")
    private String password;

    @Value("${spring.athena.region}")
    private String region;

    @Value("${spring.athena.s3-output-location}")
    private String s3OutputLocation;

    @Value("${spring.athena.database}")
    private String database;

    @Value("${spring.athena.workgroup}")
    private String workgroup;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:athena://");
        config.setDriverClassName("com.amazon.athena.jdbc.AthenaDriver");
        config.addDataSourceProperty("User", user);
        config.addDataSourceProperty("Password", password);
        config.addDataSourceProperty("Region", region);
        config.addDataSourceProperty("Database", database);
        config.addDataSourceProperty("OutputLocation", s3OutputLocation);
        return new HikariDataSource(config);
    }


    @Bean
    public FirehoseClient firehoseClient() {
        return FirehoseClient.builder()
                .region(FirehoseConstants.REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        user,
                        password)))
                .build();
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() {
        return new DataSourceConnectionProvider(dataSource());
    }

    @Bean
    public DSLContext dsl()
            throws SQLException {

        // Configure jOOQ
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        jooqConfiguration.set(SQLDialect.DEFAULT);

        // Create context
        DSLContext ctx = DSL.using(jooqConfiguration);

        return ctx;
    }


    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }


    public static class ExceptionTranslator extends DefaultExecuteListener {
        public void exception(ExecuteContext context) {
            SQLDialect dialect = context.configuration().dialect();
            SQLExceptionTranslator translator
                    = new SQLErrorCodeSQLExceptionTranslator(dialect.name());
            context.exception(translator.translate(
                    "Access database using Jooq",
                    context.sql(),
                    context.sqlException())
            );
        }
    }
}

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

package org.n52.sta.data.cloudnative.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.*;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.impl.DSL;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.schema.DefaultSchema;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.GetTableRequest;
import software.amazon.awssdk.services.glue.model.GetTableResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class TestDatabaseConfig {
    String keyId;
    String secret;
    String region;
    String endpoint;
    String url_style;
    String use_ssl;
    String database;
    String s3_output_location;

    public TestDatabaseConfig() {
        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.blob.properties")) {
            properties.load(is);
            // Retrieve properties
            keyId = properties.getProperty("keyId");
            secret = properties.getProperty("secret");
            region = properties.getProperty("region");
            endpoint = properties.getProperty("endpoint");
            url_style = properties.getProperty("url_style");
            use_ssl = properties.getProperty("use_ssl");
            database = properties.getProperty("database");
            s3_output_location = properties.getProperty("output_location");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:athena://");
        config.setDriverClassName("com.amazon.athena.jdbc.AthenaDriver");
        config.addDataSourceProperty("User", keyId);
        config.addDataSourceProperty("Password", secret);
        config.addDataSourceProperty("Region", region);
        config.addDataSourceProperty("Database", database);
        config.addDataSourceProperty("OutputLocation", s3_output_location);
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        // Configure jOOQ
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        Settings settings = new Settings().withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
        jooqConfiguration.set(settings);
        jooqConfiguration.set(dataSource);
        jooqConfiguration.set(SQLDialect.DEFAULT);
        DSLContext ctx =  DSL.using(jooqConfiguration);
        return ctx;
    }

    @Bean
    public FirehoseClient firehoseClient() {
        return FirehoseClient.builder()
                .region(FirehoseConstants.REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(keyId, secret)))
                .build();
    }
}


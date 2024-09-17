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

import org.jooq.*;
import org.jooq.impl.*;

import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.schema.DefaultSchema;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties
@PropertySource("classpath:cloudnative-datasource.yml")
public class PersistenceContext {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() {
        return new TransactionAwareDataSourceProxy(dataSource());
    }

    @Bean
    public FirehoseClient firehoseClient() {
         return FirehoseClient.builder()
                .region(FirehoseConstants.REGION)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() {
        return new DataSourceConnectionProvider(transactionAwareDataSource());
    }

    @Bean
    public DSLContext dsl() throws SQLException {

        // Configure jOOQ
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        jooqConfiguration.set(SQLDialect.DUCKDB);
        // Register the inline VisitListener
        jooqConfiguration.set(new VisitListener() {

            private final Set<Class<?>> StaTableClasses = DefaultSchema.DEFAULT_SCHEMA.getTables()
                    .stream()
                    .map(Table::getClass)
                    .collect(Collectors.toSet());

            @Override
            public void visitStart(VisitContext context) {
                QueryPart part = context.queryPart();
                if (part instanceof Table &&
                        !(part instanceof Field) &&
                        StaTableClasses.contains((part.getClass()))) {
                    handleTable(context, (Table<?>) part);
                }
            }

            private void handleTable(VisitContext context, Table<?> table) {
                String parquetReadFunction = String.format("read_parquet('s3://52n-sta/%s')", table.getName());
                Table<?> aliasedTable = DSL.table(parquetReadFunction).as(table.getName());
                context.queryPart(aliasedTable);
            }
        });

        // Create context
        DSLContext ctx = DSL.using(jooqConfiguration);

        // Load Extensions
        installAndLoadSpatialExtension(ctx);
        installLoadAndConfigureHTTPFSExtension(ctx);

        return ctx;
    }


    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }

    private void installAndLoadSpatialExtension(DSLContext ctx) {
        ctx.execute("INSTALL spatial;");
        ctx.execute("LOAD spatial;");
        System.out.println("Spatial extension loaded successfully.");
    }

    private void installLoadAndConfigureHTTPFSExtension(DSLContext ctx) {
        ctx.execute("INSTALL httpfs;");
        ctx.execute("LOAD httpfs;");

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("blob.properties")) {
            properties.load(fis);

            // Retrieve properties
            String type = properties.getProperty("type");
            String keyId = properties.getProperty("keyId");
            String secret = properties.getProperty("secret");
            String region = properties.getProperty("region");

            // Format the CREATE SECRET statement
            String createSecretStatement = String.format(
                    "CREATE SECRET secret1 (" +
                            "    TYPE %s," +
                            "    KEY_ID '%s'," +
                            "    SECRET '%s'," +
                            "    REGION '%s'" +
                            ");",
                    type, keyId, secret, region
            );
            ctx.execute(createSecretStatement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

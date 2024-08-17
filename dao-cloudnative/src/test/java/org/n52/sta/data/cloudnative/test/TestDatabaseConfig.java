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
import org.jooq.*;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.impl.DSL;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.n52.sta.data.cloudnative.schema.DefaultSchema;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class TestDatabaseConfig {

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:duckdb:")
                .driverClassName("org.duckdb.DuckDBDriver")
                .build();
    }

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        // Configure jOOQ
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();

        Settings settings = new Settings().withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
        jooqConfiguration.set(settings);
        jooqConfiguration.set(dataSource);
        jooqConfiguration.set(SQLDialect.DUCKDB);
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
                String parquetReadFunction = String.format("read_parquet('s3://52n-sta/%s.parquet')", table.getName());
                Table<?> aliasedTable = DSL.table(parquetReadFunction).as(table.getName());
                context.queryPart(aliasedTable);
            }
        });
        DSLContext ctx =  DSL.using(jooqConfiguration);
        installAndLoadSpatialExtension(ctx);
        installLoadAndConfigureHTTPFSExtension(ctx);
        return ctx;
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
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.blob.properties")) {
            properties.load(is);

            // Retrieve properties
            String keyId = properties.getProperty("keyId");
            String secret = properties.getProperty("secret");
            String region = properties.getProperty("region");
            String endpoint = properties.getProperty("endpoint");
            String url_style = properties.getProperty("url_style");
            String use_ssl = properties.getProperty("use_ssl");

            // Format the CREATE SECRET statement
            String createSecretStatement = String.format(
                    "SET s3_access_key_id='%s';" +
                    "SET s3_secret_access_key='%s';" +
                    "SET s3_endpoint='%s';" +
                    "SET s3_region='%s';" +
                    "SET s3_url_style='%s';" +
                    "SET s3_use_ssl='%s';",
                    keyId, secret, endpoint, region, url_style, use_ssl
            );
            ctx.execute(createSecretStatement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readSchema(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch(IOException e) {
            return null;
        }
    }
}


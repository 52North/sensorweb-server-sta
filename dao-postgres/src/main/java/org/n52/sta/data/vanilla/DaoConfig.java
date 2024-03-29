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
package org.n52.sta.data.vanilla;

import org.hibernate.boot.model.TypeContributor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.TypeContributorList;
import org.hibernate.type.BasicType;
import org.n52.hibernate.type.SmallBooleanType;
import org.n52.sta.data.vanilla.repositories.MessageBusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Configuration
@EnableJpaRepositories(repositoryBaseClass = MessageBusRepository.class,
                       basePackages = {"org.n52.sta.data.vanilla.repositories"})
@EnableTransactionManagement
@ConfigurationProperties(prefix = "dao-postgres")
public class DaoConfig {

    @Value("${database.jpa.persistence-location}")
    private String persistenceXmlLocation;

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource datasource, JpaProperties properties) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(addCustomTypes(properties));
        emf.setPersistenceXmlLocation(persistenceXmlLocation);
        emf.setDataSource(datasource);
        emf.afterPropertiesSet();
        return emf.getNativeEntityManagerFactory();
    }

    private Map<String, Object> addCustomTypes(JpaProperties jpaProperties) {
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        properties.put(EntityManagerFactoryBuilderImpl.TYPE_CONTRIBUTORS, createTypeContributorsList());
        return properties;
    }

    private TypeContributorList createTypeContributorsList() {
        return () -> Arrays.asList(toTypeContributor(SmallBooleanType.INSTANCE, "small_boolean"));
    }

    private <T extends BasicType> TypeContributor toTypeContributor(T type, String... keys) {
        return (typeContributions, serviceRegistry) -> typeContributions.contributeType(type, keys);
    }

}

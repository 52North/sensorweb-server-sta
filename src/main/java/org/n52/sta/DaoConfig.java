/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.sta;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.boot.model.TypeContributor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.spi.TypeContributorList;
import org.hibernate.type.BasicType;
import org.n52.hibernate.type.SmallBooleanType;
import org.n52.io.handler.DefaultIoFactory;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.AnnotationBasedDataRepositoryFactory;
import org.n52.series.db.DataRepositoryTypeFactory;
import org.n52.series.db.old.dao.DbQueryFactory;
import org.n52.series.db.old.dao.DefaultDbQueryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Configuration
public class DaoConfig {

    @Value("META-INF/persistence.xml")
    private String persistenceXmlLocation;
    
    @Bean
    public DbQueryFactory dbQueryFactory(@Value("${database.srid:'EPSG:4326'}") String srid) {
        return new DefaultDbQueryFactory(srid);
    }
    
    @Bean
    public DataRepositoryTypeFactory dataRepositoryFactory(ApplicationContext appContext) {
        return new AnnotationBasedDataRepositoryFactory(appContext);
    }
    
    @Bean
    public DefaultIoFactory<DatasetOutput<AbstractValue< ? >>, AbstractValue< ? >> defaultIoFactory() {
        return new DefaultIoFactory<>();
    }
    
    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource datasource, JpaProperties properties)
            throws IOException {
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
        return (TypeContributorList) () -> Arrays.asList(toTypeContributor(SmallBooleanType.INSTANCE, "small_boolean"));
    }

    private <T extends BasicType> TypeContributor toTypeContributor(T type, String... keys) {
        return (typeContributions, serviceRegistry) -> typeContributions.contributeType(type, keys);
    }
    
}

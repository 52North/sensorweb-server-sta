/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.api.domain.aggregate;

import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;

public class FeatureOfInterestAggregate extends EntityAggregate<FeatureOfInterest> implements FeatureOfInterest {

    private final FeatureOfInterest featureOfInterest;

    public FeatureOfInterestAggregate(FeatureOfInterest featureOfInterest,
                                      DomainService<FeatureOfInterest> domainService) {
        this(featureOfInterest, domainService, null);
    }

    public FeatureOfInterestAggregate(FeatureOfInterest featureOfInterest,
                                      DomainService<FeatureOfInterest> domainService,
                                      EntityEditor<FeatureOfInterest> editor) {
        super(featureOfInterest, domainService, editor);
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public String getId() {
        return featureOfInterest.getId();
    }

    @Override
    public String getName() {
        return featureOfInterest.getName();
    }

    @Override
    public String getDescription() {
        return featureOfInterest.getDescription();
    }

    @Override
    public Map<String, Object> getProperties() {
        return featureOfInterest.getProperties();
    }

    @Override
    public Geometry getFeature() {
        return featureOfInterest.getFeature();
    }

    @Override
    public Set<Observation> getObservations() {
        return featureOfInterest.getObservations();
    }
}

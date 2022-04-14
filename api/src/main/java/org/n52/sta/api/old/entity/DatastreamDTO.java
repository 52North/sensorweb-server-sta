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
package org.n52.sta.api.old.entity;

import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.old.dto.common.StaDTO;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface DatastreamDTO extends StaDTO {

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getObservationType();

    void setObservationType(String observationType);

    UnitOfMeasurement getUnitOfMeasurement();

    void setUnitOfMeasurement(UnitOfMeasurement uom);

    Geometry getObservedArea();

    void setObservedArea(Geometry ObservedArea);

    Time getPhenomenonTime();

    void setPhenomenonTime(Time phenomenonTime);

    Time getResultTime();

    void setResultTime(Time resultTimeStart);

    ObjectNode getProperties();

    void setProperties(ObjectNode properties);

    ThingDTO getThing();

    void setThing(ThingDTO thing);

    SensorDTO getSensor();

    void setSensor(SensorDTO sensor);

    ObservedPropertyDTO getObservedProperty();

    void setObservedProperty(ObservedPropertyDTO observedProperty);

    Set<ObservationDTO> getObservations();

    void setObservations(Set<ObservationDTO> observations);

    class UnitOfMeasurement {

        private String symbol;
        private String name;
        private String definition;

        public UnitOfMeasurement() {

        }

        public UnitOfMeasurement(String symbol, String name, String definition) {
            this.symbol = symbol;
            this.name = name;
            this.definition = definition;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}

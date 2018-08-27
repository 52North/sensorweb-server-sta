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
package org.n52.sta.mapping;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_PHENOMENON_TIME;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_RESULT_TIME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ES_OBSERVATIONS_NAME;
import static org.n52.sta.edm.provider.entities.ObservationEntityProvider.ET_OBSERVATION_FQN;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.ComplexDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryDataEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ReferencedDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservationMapper {

	@Autowired
	EntityCreationHelper entityCreationHelper;

	public Entity createObservationEntity(DataEntity observation) {
		Entity entity = new Entity();
		entity.addProperty(new Property(null, ID_ANNOTATION, ValueType.PRIMITIVE, observation.getId()));

		//TODO: check observation type, cast and get value
		entity.addProperty(new Property(null, PROP_RESULT, ValueType.PRIMITIVE, this.getResult(observation)));


		SimpleDateFormat converter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		entity.addProperty(new Property(null, PROP_RESULT_TIME, ValueType.PRIMITIVE, converter.format(observation.getResultTime())));

		String phenomenonTime = (observation.getSamplingTimeStart().equals(observation.getSamplingTimeEnd())) ?
				converter.format(observation.getSamplingTimeStart()) :
				converter.format(observation.getSamplingTimeStart()) + "/" + converter.format(observation.getSamplingTimeEnd());
		entity.addProperty(new Property(null, PROP_PHENOMENON_TIME, ValueType.PRIMITIVE, phenomenonTime));

		entity.setType(ET_OBSERVATION_FQN.getFullQualifiedNameAsString());
		entity.setId(entityCreationHelper.createId(entity, ES_OBSERVATIONS_NAME, ID_ANNOTATION));

		return entity;
	}

	private String getResult(DataEntity o) {
		if (o instanceof QuantityDataEntity) {
			return 	((QuantityDataEntity)o).getValue().toString();
		} else if (o instanceof BlobDataEntity) {
			// TODO: check if Object.tostring is what we want here
			return ((BlobDataEntity)o).getValue().toString();
		} else if (o instanceof BooleanDataEntity) {
			return ((BooleanDataEntity)o).getValue().toString();
		} else if (o instanceof CategoryDataEntity) {
			return ((CategoryDataEntity)o).getValue();
		} else if (o instanceof ComplexDataEntity) {

			//TODO: implement
			//return ((ComplexDataEntity)o).getValue();
			return null;

		} else if (o instanceof CountDataEntity) {
			return ((CountDataEntity)o).getValue().toString();
		} else if (o instanceof GeometryDataEntity) {

			//TODO: check if we want WKT here
			return ((GeometryDataEntity)o).getValue().getGeometry().toText();

		} else if (o instanceof TextDataEntity) {
			return ((TextDataEntity)o).getValue();
		} else if (o instanceof DataArrayDataEntity) {

			//TODO: implement
			//return ((DataArrayDataEntity)o).getValue();
			return null;

		} else if (o instanceof ProfileDataEntity) {

			//TODO: implement
			//return ((ProfileDataEntity)o).getValue();
			return null;

		} else if (o instanceof ReferencedDataEntity) {
			return ((ReferencedDataEntity)o).getValue();
		}
		return "";
	}
}

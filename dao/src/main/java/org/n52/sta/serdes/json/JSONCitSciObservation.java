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

package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.n52.sta.api.dto.CitSciObservationDTO;
import org.n52.sta.api.dto.ObservationGroupDTO;
import org.n52.sta.api.dto.ObservationRelationDTO;
import org.n52.sta.api.dto.impl.CitSciObservation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONCitSciObservation extends AbstractJSONObservation<CitSciObservationDTO> {

    @JsonManagedReference
    public JSONObservationGroup[] ObservationGroups;

    @JsonManagedReference
    public JSONObservationRelation[] Subjects;

    @JsonManagedReference
    public JSONObservationRelation[] Objects;

    public JSONCitSciObservation() {
        self = new CitSciObservation();
    }

    public CitSciObservationDTO parseToDTO(JSONBase.EntityType type) {
        super.parseToDTO(type);

        if (Subjects != null) {
            Set<ObservationRelationDTO> subjects = new HashSet<>();
            for (JSONObservationRelation sub : Subjects) {
                subjects.add(sub.parseToDTO(JSONBase.EntityType.FULL,
                                            JSONBase.EntityType.REFERENCE));
            }
            self.setSubjects(subjects);
        }

        if (Objects != null) {
            Set<ObservationRelationDTO> objects = new HashSet<>();
            for (JSONObservationRelation obj : Objects) {
                objects.add(obj.parseToDTO(JSONBase.EntityType.FULL,
                                           JSONBase.EntityType.REFERENCE));
            }
            self.setObjects(objects);
        }

        if (ObservationGroups != null) {
            Set<ObservationGroupDTO> objects = new HashSet<>();
            for (JSONObservationGroup obj : ObservationGroups) {
                objects.add(obj.parseToDTO(JSONBase.EntityType.FULL,
                                           JSONBase.EntityType.REFERENCE));
            }
            self.setGroups(objects);
        }
        return self;
    }
}

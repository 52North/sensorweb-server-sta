/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@SuppressWarnings("VisibilityModifier")
public class JSONBase {

    public enum EntityType {
        FULL,
        PATCH,
        REFERENCE
    }

    abstract static class JSONwithId<T> {

        private static Logger LOGGER = LoggerFactory.getLogger(JSONwithId.class);

        @JsonProperty("@iot.id")
        public String identifier = UUID.randomUUID().toString();

        @JsonBackReference
        public Object backReference;

        // Deals with linking to parent Objects during deep insert
        // Used for dealing with nested inserts
        protected T self;

        protected boolean generatedId = true;

        public void setIdentifier(String rawIdentifier) throws UnsupportedEncodingException {
            generatedId = false;
            Assert.doesNotContain(rawIdentifier, "/", "Identifier may not contain slashes due to incompatibility " +
                    "with @iot.selfLink!");
            identifier = rawIdentifier;
        }

        /**
         * Returns a reference to the result of this classes toEntity() method
         *
         * @return reference to created database entity
         */
        public T getEntity() {
            Assert.notNull(self, "Trying to get Entity prior to creation!");
            return this.self;
        }

        /**
         * Creates and validates the Database Entity to conform to invariants defined in standard.
         * What is validated is dictated by given type parameter
         *
         * @param type type of the entity
         * @return created Entity
         */
        public abstract T toEntity(EntityType type);

        /**
         * Used when multiple Entity Types are allowed.
         *
         * @param type1 first type to check
         * @param type2 second type to check
         * @return created entity
         */
        public T toEntity(EntityType type1, EntityType type2) {
            try {
                return toEntity(type1);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                try {
                    return toEntity(type2);
                } catch (IllegalStateException | IllegalArgumentException secondEx) {
                    throw new IllegalStateException(
                            type1.name() + ex.getMessage() + type2.name() + secondEx.getMessage());
                }
            }
        }
    }


    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    abstract static class JSONwithIdNameDescription<T> extends JSONwithId<T> {
        public String name;
        public String description;
    }


    @SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    abstract static class JSONwithIdNameDescriptionTime<T> extends JSONwithIdTime<T> {
        public String name;
        public String description;
    }


    abstract static class JSONwithIdTime<T> extends JSONwithId<T> {

        protected Time createTime(DateTime time) {
            return new TimeInstant(time);
        }

        /**
         * Create {@link Time} from {@link DateTime}s
         *
         * @param start Start {@link DateTime}
         * @param end   End {@link DateTime}
         * @return Resulting {@link Time}
         */
        protected Time createTime(DateTime start, DateTime end) {
            if (start.equals(end)) {
                return createTime(start);
            } else {
                return new TimePeriod(start, end);
            }
        }

        protected Time parseTime(String input) {
            if (input.contains("/")) {
                String[] split = input.split("/");
                return createTime(DateTime.parse(split[0]),
                                  DateTime.parse(split[1]));
            } else {
                return new TimeInstant(DateTime.parse(input));
            }
        }

    }

}

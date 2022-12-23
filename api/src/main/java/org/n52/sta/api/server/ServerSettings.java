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

package org.n52.sta.api.server;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface ServerSettings {

    void addConformanceClass(String conformanceClass);

    void addExtension(String name, Extension extension);

    List<String> getConformanceClasses();

    Map<String, Extension> getExtensions();

    Set<EntitySet> getEntitySets();

    interface Extension {
        Map<String, Object> getProperties();
    }

    final class EntitySet {

        private String name;

        private String url;

        EntitySet(String name, String url) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(url, "url must not be null");
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }
        
        public String getUrl() {
            return url;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(9, getName());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntitySet other = (EntitySet) obj;
            return Objects.equals(this.getName(), other.getName());
        }

    }

}

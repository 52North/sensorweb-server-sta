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

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
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
            return Objects.equals(this.name, other.name);
        }


    }

}

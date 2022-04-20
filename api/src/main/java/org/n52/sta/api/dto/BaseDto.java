package org.n52.sta.api.dto;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseDto extends StaDto {
     
    private String name;

    private String description;

    private Map<String, Object> properties;

    protected BaseDto() {
        this.properties = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

}

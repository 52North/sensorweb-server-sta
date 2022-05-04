package org.n52.sta.http.serialize.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public interface StaSerializer<T> {

    public static final String SELECT_FILTER = "selectFilter";

    Class<T> getType();
    
    /**
     * Registers at the specified object mapper.
     * <p>
     * The serializer instance has to respect dynamic query options. For 
     * that reason it the instance has to be registered as a {@link SimpleModule}
     * at the specified object mapper. For this, you have to copy the object
     * mapper for each request:
     * 
     * <pre>
     *   ObjectMapper requestMapper = mapperConfig.copy();
     *   
     *   // ... create serializer
     * 
     *   serializer.registerAt(requestMapper);
     * </pre>
     * 
     * @param mapper the mapper where to register the module.
     */
    void registerAt(ObjectMapper mapper);
}

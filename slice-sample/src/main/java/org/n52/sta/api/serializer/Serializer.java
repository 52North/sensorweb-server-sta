package org.n52.sta.api.serializer;

@FunctionalInterface
public interface Serializer<T, S> {
    
    S serialize(T toSerialize);
}

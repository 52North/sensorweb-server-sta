package org.n52.sta.http.serialize.out;

public interface StaSerializer<T> {

    public static final String SELECT_FILTER = "selectFilter";

    Class<T> getType();
    
}

package org.n52.sta.data.cloudnative.dao;
import software.amazon.awssdk.regions.Region;

public interface FirehoseConstants {

    Region REGION = Region.US_EAST_1;
    String DELIVERY_STREAM_NAME = "sta-iceberg";

    String TABLE = "table";
    String OPERATION = "operation";
    String DATA = "data";
    String DELETE_KEY = "delete_key";
    String DELETE_VALUE = "delete_value";

    String INSERT = "INSERT";
    String UPDATE = "UPDATE";
    String DELETE = "DELETE";
}

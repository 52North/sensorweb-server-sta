package org.n52.sta.data.cloudnative.dao;
import software.amazon.awssdk.regions.Region;

public interface FirehoseConstants {

    Region REGION = Region.US_EAST_1;
    String DELIVERY_STREAM_NAME = "52N-STA-DF-ICBG";

    String TABLE = "DestinationTableName";
    String DATABASE = "DestinationDatabaseName";
    String DATABASE_NAME = "52n_sta_iceberg";
    String OPERATION = "Operation";

    String ADF_RECORD = "ADF_Record";
    String ADF_METADATA = "ADF_Metadata";
    String OTF_METADATA = "OTF_Metadata";

    String INSERT = "INSERT";
    String UPDATE = "UPDATE";
    String DELETE = "DELETE";
}

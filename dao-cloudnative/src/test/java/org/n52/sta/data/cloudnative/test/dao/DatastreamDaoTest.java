package org.n52.sta.data.cloudnative.test.dao;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.FirehoseConstants;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Dataset;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class DatastreamDaoTest {
    private DSLContext ctx;
    private final DatastreamDaoImpl datastreamDao;
    private FirehoseClient firehoseClient;
    private StaFirehoseClient staFirehose = null;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public DatastreamDaoTest(DSLContext ctx, FirehoseClient firehoseClient) throws STACRUDException {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        staFirehose = new StaFirehoseClient(firehoseClient);
        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose);
    }
    @Test
    public void save() throws STACRUDException {
        Dataset datasetPOJO = new Dataset();
        datasetPOJO.setName("oven temperature");
        datasetPOJO.setDescription("This is a datastream for an oven’s internal temperature.");
        datasetPOJO.setDatasetId(123456L);
        datasetPOJO.setIdentifier("2006");
        datasetPOJO.setStaIdentifier("2006");
        datasetPOJO.setFirstTime(LocalDateTime.of(2020, 6, 26, 9, 42, 2));
        datasetPOJO.setLastTime(LocalDateTime.of(2021, 6, 26, 9, 42, 2));
        datasetPOJO.setFkProcedureId(1L);
        datasetPOJO.setFkPhenomenonId(1L);
        datasetPOJO.setFkFeatureId(1L);
        datasetPOJO.setFkUnitId(1L);
        datasetPOJO.setFkFormatId(5L);
        datasetPOJO.setFkPlatformId(2L);
        datasetPOJO.setObservationType("simple");

        datastreamDao.save(datasetPOJO);

        //ctx.execute("INSERT INTO \"52n_sta_iceberg\".\"dataset\" (dataset_id, identifier, sta_identifier,name,description,first_time,last_time,fk_procedure_id,fk_phenomenon_id,fk_platform_id,fk_unit_id,fk_format_id,fk_feature_id, observation_type) VALUES (BIGINT '999', '999', '999', VARCHAR 'oven temperature', VARCHAR 'This is a datastream for an oven’s internal temperature.', TIMESTAMP '2024-03-25 11:12:13', TIMESTAMP '2024-03-25 11:12:13', 1, 1, 2, 1, 5, 1, 'simple');");

    }

    @Test
    public void testWithExistsByName() {
        String name = "oven temperature";
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        boolean result = false;
        try {
            result = datastreamDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }


    @Test
    public void testWithFindByName() {
        String name = "oven temperature";
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        Optional<DatastreamDTO> result = Optional.empty();
        try {
            result = datastreamDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), "oven temperature");
    }

    @Test
    public void testWithGetColumn() {
        String name = "observation_type";
        String value = "simple";
        Condition condition = DSL.field(name).eq(value);
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = datastreamDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 1L;
        String description = "This is a datastream for an oven’s internal temperature.";
        QueryOptions options = null;
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        Optional<DatastreamDTO> result = Optional.empty();
        try {
            result = datastreamDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindOne() {
        String description = "This is a datastream for an oven’s internal temperature.";
        Condition predicate = StaEntity.DATASTREAM.DESCRIPTION.eq(description);
        QueryOptions options = null;
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        Optional<DatastreamDTO> result = Optional.empty();
        try {
            result = datastreamDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindAll() {
        String value = "degree Celsius";
        Condition predicate = StaEntity.UNIT.NAME.eq("degree Celsius");
        QueryOptions options = null;
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        List<DatastreamDTO> result = List.of();
        try {
            result = datastreamDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getUnitOfMeasurement().getName(), value);
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "1";
        QueryOptions options = null;
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        Optional<DatastreamDTO> result = Optional.empty();
        try {
            result = datastreamDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "1";
        Class<DatastreamDTO> entityClass = DatastreamDTO.class;
        boolean result = false;
        try {
            result = datastreamDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }
}

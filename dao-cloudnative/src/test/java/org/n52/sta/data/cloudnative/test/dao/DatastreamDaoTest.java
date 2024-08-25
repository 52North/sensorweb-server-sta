package org.n52.sta.data.cloudnative.test.dao;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class DatastreamDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private DatastreamDaoImpl datastreamDao;

    @BeforeEach
    public void setUp() {
        datastreamDao = new DatastreamDaoImpl();
        datastreamDao.setCtx(ctx);
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
        String identifier = "b240f688-08d9-41e3-8f47-63de72a9fd40";
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
        String identifier = "b240f688-08d9-41e3-8f47-63de72a9fd40";
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

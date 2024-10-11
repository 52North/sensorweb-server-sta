package org.n52.sta.data.cloudnative.test.dao;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.cloudnative.condition.FeatureOfInterestQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.FeatureOfInterestDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class FeatureOfInterestDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private FeatureOfInterestDaoImpl featureDao;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose = new StaFirehoseClient(firehoseClient);
    private final FeatureOfInterestQueryConditions foiQC = new FeatureOfInterestQueryConditions();

    @BeforeEach
    public void setUp() {
        foiQC.setDslContext(ctx);
        featureDao = new FeatureOfInterestDaoImpl(ctx, staFirehose, foiQC);
        ctx.execute("INSERT INTO \"52n_sta_iceberg\".\"feature\" " +
                "(feature_id, sta_identifier, identifier, fk_format_id, name, description, geom) " +
                "VALUES (BIGINT '1', " +
                "VARCHAR '1', " +
                "VARCHAR '1', " +
                "BIGINT '12', " +
                "VARCHAR 'CCIT #361', " +
                "VARCHAR 'This is CCIT #361, Noah’s dad’s office', " +
                "from_hex('010300000001000000050000000000000000003e4000000000000024400000000000004440000000000000444000000000000034400000000000004440000000000000244000000000000034400000000000003e400000000000002440'));");
    }

    @Test
    public void testWithExistsByName() {
        String name = "CCIT #361";
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        boolean result = false;
        try {
            result = featureDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }

    @Test
    public void testWithFindByName() {
        String name = "CCIT #361";
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<FeatureOfInterestDTO> result = Optional.empty();
        try {
            result = featureDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), "CCIT #361");
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "1";
        Condition condition = DSL.field(name).eq(value);
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = featureDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 1L;
        String description = "This is CCIT #361, Noah’s dad’s office";
        QueryOptions options = null;
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<FeatureOfInterestDTO> result = Optional.empty();
        try {
            result = featureDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindOne() {
        String geom = "POLYGON ((100 50, 10 9, 23 4, 100 50), (30 20, 10 4, 4 22, 30 20))";
        Condition predicate = DSL.function("ST_AsText", String.class,
                DSL.function("ST_GeomFromBinary", byte[].class, StaEntity.FEATURE_OF_INTEREST.GEOM)).eq(geom);
        QueryOptions options = null;
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<FeatureOfInterestDTO> result = Optional.empty();
        try {
            result = featureDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "1");
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.FEATURE_PROPERTIES.NAME.eq("foi_code");
        QueryOptions options = null;
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        List<FeatureOfInterestDTO> result = List.of();
        try {
            result = featureDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getProperties().findValue("foi_code").toString(),
                "\"foi.001.sample\"");
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "1";
        QueryOptions options = null;
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<FeatureOfInterestDTO> result = Optional.empty();
        try {
            result = featureDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "1";
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        boolean result = false;
        try {
            result = featureDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }

}

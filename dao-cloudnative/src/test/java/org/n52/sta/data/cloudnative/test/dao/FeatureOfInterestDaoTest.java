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
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.FeatureOfInterestDaoImpl;
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
public class FeatureOfInterestDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private FeatureOfInterestDaoImpl featureDao;

    @BeforeEach
    public void setUp() {
        featureDao = new FeatureOfInterestDaoImpl();
        featureDao.setCtx(ctx);
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
        String value = "60a6ad14-1730-4d75-aa34-fce7795470ce";
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
                DSL.function("ST_GeomFromWKB", byte[].class, StaEntity.FEATURE_OF_INTEREST.GEOM)).eq(geom);
        QueryOptions options = null;
        Class<FeatureOfInterestDTO> entityClass = FeatureOfInterestDTO.class;
        Optional<FeatureOfInterestDTO> result = Optional.empty();
        try {
            result = featureDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "60a6ad14-1730-4d75-aa34-fce7795470ce");
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
        String identifier = "60a6ad14-1730-4d75-aa34-fce7795470ce";
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
        String identifier = "60a6ad14-1730-4d75-aa34-fce7795470ce";
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

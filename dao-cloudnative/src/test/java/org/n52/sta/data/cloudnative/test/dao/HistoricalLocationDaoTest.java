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
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.HistoricalLocationDaoImpl;
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
public class HistoricalLocationDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private HistoricalLocationDaoImpl historicalLocationDao;

    @BeforeEach
    public void setUp() {
        historicalLocationDao = new HistoricalLocationDaoImpl(ctx, null);
    }
    @Test
    public void testWithGetColumn() {
        String name = "time";
        String value = "2024-08-08 21:31:53.864";
        Condition condition = DSL.field(name).eq(value);
        Class<HistoricalLocationDTO> entityClass = HistoricalLocationDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = historicalLocationDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 2L;
        String sta_identifier = "bf5bb163-5f59-4ec0-8a6e-116fb627244b";
        QueryOptions options = null;
        Class<HistoricalLocationDTO> entityClass = HistoricalLocationDTO.class;
        Optional<HistoricalLocationDTO> result = Optional.empty();
        try {
            result = historicalLocationDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), sta_identifier);
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.HISTORICAL_LOCATION.FK_PLATFORM_ID.eq(2L);
        QueryOptions options = null;
        Class<HistoricalLocationDTO> entityClass = HistoricalLocationDTO.class;
        List<HistoricalLocationDTO> result = List.of();
        try {
            result = historicalLocationDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getId(),
                "bf5bb163-5f59-4ec0-8a6e-116fb627244b");
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "bf5bb163-5f59-4ec0-8a6e-116fb627244b";
        QueryOptions options = null;
        Class<HistoricalLocationDTO> entityClass = HistoricalLocationDTO.class;
        Optional<HistoricalLocationDTO> result = Optional.empty();
        try {
            result = historicalLocationDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "bf5bb163-5f59-4ec0-8a6e-116fb627244b";
        Class<HistoricalLocationDTO> entityClass = HistoricalLocationDTO.class;
        boolean result = false;
        try {
            result = historicalLocationDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }
}


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
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.ObservationDaoImpl;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class ObservationDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private ObservationDaoImpl observationDao;

    @BeforeEach
    public void setUp() {
        observationDao = new ObservationDaoImpl();
        observationDao.setCtx(ctx);
    }

    @Test
    public void testWithFindFirstByDatasetIdOrderBySamplingTimeStartAsc() {
        Long datasetId = 1L;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        ObservationDTO observation = null;
        try {
            observation = observationDao.findFirstByDatasetIdOrderBySamplingTimeStartAsc(datasetId, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(observation.getId(), "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38");
    }

    @Test
    public void testWithFindFirstByDatasetIdOrderBySamplingTimeEndDesc() {
        Long datasetId = 1L;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        ObservationDTO observation = null;
        try {
            observation = observationDao.findFirstByDatasetIdOrderBySamplingTimeEndDesc(datasetId, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(observation.getId(), "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38");
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38";
        Condition condition = DSL.field(name).eq(value);
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = observationDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 1L;
        QueryOptions options = null;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Optional<ObservationDTO> result = Optional.empty();
        try {
            result = observationDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38");
    }

    @Test
    public void testWithFindOne() {
        String valueType = "quantity";
        Condition predicate = StaEntity.OBSERVATION.VALUE_TYPE.eq(valueType);
        QueryOptions options = null;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Optional<ObservationDTO> result = Optional.empty();
        try {
            result = observationDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getResult().toString(), "70.4");
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38";
        QueryOptions options = null;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Optional<ObservationDTO> result = Optional.empty();
        try {
            result = observationDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithFindAll() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime phenomenonTime = LocalDateTime.parse("2012-06-26T09:42:02.000", formatter);
        Condition predicate = StaEntity.OBSERVATION.SAMPLING_TIME_START.eq(phenomenonTime);
        QueryOptions options = null;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        List<ObservationDTO> result = List.of();
        try {
            result = observationDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(DateTimeHelper.format(result.get(0).getPhenomenonTime()),
                "2012-06-26T09:42:02.000Z");
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "7800368e0ff362d2924424b91d4e3d2381cc1ba9ecfd7bcd72b5d7b3c1e17e38";
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        boolean result = false;
        try {
            result = observationDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }
}

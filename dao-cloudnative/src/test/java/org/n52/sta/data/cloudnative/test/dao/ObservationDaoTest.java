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
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.ObservationDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Observation;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.sql.Timestamp;
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
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose = new StaFirehoseClient(firehoseClient);
    private final DatastreamQueryConditions dsQC = new DatastreamQueryConditions();
    private final ObservationQueryConditions oQC = new ObservationQueryConditions();

    @BeforeEach
    public void setUp() {
        dsQC.setDslContext(ctx);
        oQC.setDslContext(ctx);
        observationDao = new ObservationDaoImpl(ctx, staFirehose, dsQC, oQC);
    }

    @Test
    public void testWithFindFirstByDatasetIdOrderBySamplingTimeStartAsc() {
        Long datasetId = 1L;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Observation observation = null;
        try {
            observation = observationDao.findFirstByDatasetIdOrderBySamplingTimeStartAsc(datasetId, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(observation.getObservationId(), 1L);
    }

    @Test
    public void testWithFindFirstByDatasetIdOrderBySamplingTimeEndDesc() {
        Long datasetId = 1L;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Observation observation = null;
        try {
            observation = observationDao.findFirstByDatasetIdOrderBySamplingTimeEndDesc(datasetId, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(observation.getObservationId(), 2L);
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "101";
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
        Long id = 60001L;
        QueryOptions options = null;
        Class<ObservationDTO> entityClass = ObservationDTO.class;
        Optional<ObservationDTO> result = Optional.empty();
        try {
            result = observationDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), "60001");
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
        String identifier = "2";
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
        Timestamp phenomenonTime =  Timestamp.valueOf("2012-06-26 09:42:02");
        Condition predicate = DSL.field(StaEntity.OBSERVATION.SAMPLING_TIME_START.getName()).eq(phenomenonTime);
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
        String identifier = "101";
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

package org.n52.sta.data.cloudnative.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.ObservationDTO;
import org.n52.sta.api.dto.impl.Datastream;
import org.n52.sta.api.dto.impl.FeatureOfInterest;
import org.n52.sta.api.dto.impl.Observation;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.dao.impl.*;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.service.*;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.n52.sta.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.n52.sta.data.cloudnative.service.CloudNativeEntityServiceRepository.EntityTypes.FeatureOfInterest;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ObservationServiceTest {
    private final DSLContext ctx;
    private final FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose;
    private final MutexFactory mutex;
    private final LocationDaoImpl locationDao;
    private final ObservationDaoImpl observationDao;
    private final DatastreamDaoImpl datastreamDao;
    private final CloudNativeObservationService observationService;
    private final ObservationQueryConditions oQC;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;
    private final FeatureOfInterestDaoImpl featureDao;
    private final CloudNativeFeatureOfInterestService featureService;
    private final FormatDaoImpl formatDao;
    private final CloudNativeFormatService formatService;
    private final CloudNativeDatastreamService datastreamService;
    private final UnitDaoImpl unitDao;
    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ObservationServiceTest(FirehoseClient firehoseClient, DSLContext ctx) {
        this.firehoseClient = firehoseClient;
        this.ctx = ctx;
        this.staFirehose = new StaFirehoseClient(firehoseClient);
        this.mutex = new MutexFactory();
        observationDao = new ObservationDaoImpl(ctx, staFirehose);
        locationDao = new LocationDaoImpl(ctx, staFirehose);
        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose);
        oQC = new ObservationQueryConditions();
        oQC.setDslContext(ctx);
        CloudNativeObservationService.setObservationQueryConditions(oQC);
        observationService = new CloudNativeObservationService(observationDao,
                datastreamDao,
                locationDao,
                mutex);

        // dependencies
        featureDao = new FeatureOfInterestDaoImpl(ctx, staFirehose);
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        unitDao = new UnitDaoImpl(ctx, staFirehose);
        formatService = new CloudNativeFormatService(mutex, formatDao);
        featureService = new CloudNativeFeatureOfInterestService(formatService,
                observationDao,
                datastreamDao,
                featureDao,
                mutex);
        datastreamService = new CloudNativeDatastreamService(datastreamDao,
                formatService,
                observationDao,
                unitDao,
                mutex);

        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream))
                .thenReturn(datastreamService);
        when(mockServiceRepository.getEntityServiceRaw(FeatureOfInterest))
                .thenReturn(featureService);
        observationService.setServiceRepository(mockServiceRepository);
    }

    @Test
    @Order(1)
    public void testCreateWithCreateDatasetAggregation() {
        ObservationDTO observation = new Observation();
        observation.setId("12345");
        observation.setValidTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        observation.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("obs_code", "test_create_12345");
        observation.setParameters(properties);
        observation.setResult("1000");

        DatastreamDTO datastream = new Datastream();
        datastream.setId("2");
        observation.setDatastream(datastream);

        try {
            observation = observationService.create(observation);
            entityId = observation.getId();
            Long datasetId = Long.parseLong(observation.getDatastream().getId());
            Thread.sleep(45000);
            Assertions.assertTrue(observationDao.existsByStaIdentifier(entityId, ObservationDTO.class));
            Assertions.assertEquals(datastreamDao.findById(datasetId,null, DatastreamDTO.class)
                            .get().getId(),
                    datasetId.toString());

        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @Order(2)
    public void testCreateWithExistingDatasetAggregation() {
        ObservationDTO observation = new Observation();
        observation.setId("12345");
        observation.setValidTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        observation.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("obs_code", "test_createAggregation_12345");
        observation.setParameters(properties);
        observation.setResult("2000");

        DatastreamDTO datastream = new Datastream();
        datastream.setId("1727823726139000");
        observation.setDatastream(datastream);

        FeatureOfInterestDTO feature = new FeatureOfInterest();
        feature.setId("1727823750842000");
        observation.setFeatureOfInterest(feature);

        try {
            observation = observationService.create(observation);
            entityId = observation.getId();
            Long datasetId = Long.parseLong(observation.getDatastream().getId());
            Thread.sleep(45000);
            Assertions.assertTrue(observationDao.existsByStaIdentifier(entityId, ObservationDTO.class));
            Assertions.assertEquals(datastreamDao.findById(datasetId,null, DatastreamDTO.class)
                    .get().getId(),
                    datasetId.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testCreateWithExpandDatasetAggregation() {
        ObservationDTO observation = new Observation();
        observation.setId("54321");
        observation.setValidTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        observation.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("obs_code", "test_expandAggregation_54321");
        observation.setParameters(properties);
        observation.setResult("4000");

        DatastreamDTO datastream = new Datastream();
        datastream.setId("1727729001610000");
        observation.setDatastream(datastream);

        FeatureOfInterestDTO feature = new FeatureOfInterest();
        feature.setId("1000");
        feature.setName("New_Feature_Expand_Datastream");
        feature.setDescription("New Feature that expands Datastream");
        try {
            String wktLineString = "LINESTRING (30 10, 10 30, 40 40, 15 25)";
            Geometry geom = new WKTReader().read(wktLineString);
            feature.setFeature(geom);
        } catch (Exception e) {
            Assert.fail();
        }
        observation.setFeatureOfInterest(feature);

        try {
            observation = observationService.create(observation);
            entityId = observation.getId();
            Long datasetId = Long.parseLong(observation.getDatastream().getId());
            Thread.sleep(45000);
            Assertions.assertTrue(observationDao.existsByStaIdentifier(entityId, ObservationDTO.class));
            Assertions.assertEquals(datastreamDao.findById(datasetId,null, DatastreamDTO.class)
                            .get().getId(),
                    datasetId.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUpdate() {
        ObservationDTO observation = new Observation();
        entityId = "1727824643392000";
        observation.setId(entityId);
        Time resultTime = TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        ));
        observation.setResultTime(resultTime);
        ObjectNode properties = mapper.createObjectNode();
        properties.put("obs_code", "test_updateAggregation_" + entityId);
        observation.setParameters(properties);
        BigDecimal value = BigDecimal.valueOf(2024.0);
        observation.setResult(value);
        Time validTime = TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        ));
        observation.setValidTime(validTime);
        Time phenomenonTime = TimeUtil.createTime(TimeUtil.createDateTime(Timestamp.from(Instant.now())));
        observation.setPhenomenonTime(phenomenonTime);
        try {
            observationService.update(entityId, observation, HttpMethod.PATCH);
            Thread.sleep(45000);

            ObservationDTO merged = observationDao
                    .findById(Long.valueOf(entityId), null, ObservationDTO.class).get();

            Time mergedResultTime = merged.getResultTime();
            Time mergedPhenomenonTime = merged.getPhenomenonTime();
            Number mergedResultValue = (Number) merged.getResult();

            Assertions.assertEquals(mergedResultTime, resultTime);
            Assertions.assertEquals(mergedResultValue, value);
            Assertions.assertEquals(mergedPhenomenonTime, phenomenonTime);

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDelete() {
        try {
            entityId = "1727835218334000";
            observationService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(observationDao.existsByStaIdentifier(entityId, ObservationDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

}

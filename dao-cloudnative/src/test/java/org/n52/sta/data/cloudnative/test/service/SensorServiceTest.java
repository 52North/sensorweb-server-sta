package org.n52.sta.data.cloudnative.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.impl.Sensor;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;
import org.n52.sta.data.cloudnative.condition.SensorQueryConditions;
import org.n52.sta.data.cloudnative.dao.impl.*;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.service.*;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.n52.sta.api.RequestUtils.QUERY_OPTIONS_FACTORY;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SensorServiceTest {
    private DSLContext ctx;
    private FirehoseClient firehoseClient;
    private DatastreamDaoImpl datastreamDao;
    private StaFirehoseClient staFirehose;
    private FormatDaoImpl formatDao;
    private CloudNativeFormatService formatService;
    private SensorDaoImpl sensorDao;
    private CloudNativeSensorService sensorService;
    private final MutexFactory mutex = new MutexFactory();
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;
    private SensorQueryConditions sQC;
    private DatastreamQueryConditions dQC;
    // dependencies
    private final ObservationDaoImpl observationDao;
    private final ObservationQueryConditions oQC;
    private final UnitDaoImpl unitDao;
    private final LocationDaoImpl locationDao;
    private final CloudNativeDatastreamService datastreamService;
    private final CloudNativeObservationService observationService;
    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public SensorServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);

        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose);
        sensorDao = new SensorDaoImpl(ctx, staFirehose);
        formatDao = new FormatDaoImpl(ctx, staFirehose);

        formatService = new CloudNativeFormatService(mutex, formatDao);
        sensorService = new CloudNativeSensorService(sensorDao, datastreamDao, formatService, mutex);

        sQC = new SensorQueryConditions();
        dQC = new DatastreamQueryConditions();
        dQC.setDslContext(ctx);
        sQC.setDslContext(ctx);
        CloudNativeSensorService.setSensorQueryConditions(sQC);
        CloudNativeSensorService.setDatastreamQueryConditions(dQC);

        // dependencies
        oQC = new ObservationQueryConditions();
        oQC.setDslContext(ctx);
        observationDao = new ObservationDaoImpl(ctx, staFirehose);
        unitDao = new UnitDaoImpl(ctx, staFirehose);
        locationDao = new LocationDaoImpl(ctx, staFirehose);
        datastreamService = new CloudNativeDatastreamService(
                datastreamDao,
                formatService,
                observationDao,
                unitDao,
                mutex
        );
        observationService = new CloudNativeObservationService(observationDao,
                datastreamDao,
                locationDao,
                mutex);
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream))
                .thenReturn(datastreamService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Observation))
                .thenReturn(observationService);
        sensorService.setServiceRepository(mockServiceRepository);
        datastreamService.setServiceRepository(mockServiceRepository);
        CloudNativeDatastreamService.setDatastreamQueryConditions(dQC);
        CloudNativeObservationService.setObservationQueryConditions(oQC);
    }

    @Test
    @Order(1)
    public void testCreate() {
        SensorDTO sensor = new Sensor();
        sensor.setId("54321");
        sensor.setEncodingType("application/pdf");
        sensor.setDatastreams(null);
        sensor.setMetadata("random.metadata.com/sensor_54321");
        sensor.setName("test_sensor_54321");
        sensor.setDescription("some description");
        ObjectNode properties = mapper.createObjectNode();
        properties.put("sensor_date", "09.25.2024");
        sensor.setProperties(properties);
        try {
             entityId = sensorService.create(sensor).getId();
            Thread.sleep(45000);
             Assertions.assertTrue(sensorDao.existsByName("test_sensor_54321", SensorDTO.class));
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    @Order(2)
    public void testUpdate() {
        SensorDTO sensor = new Sensor();
        sensor.setId(entityId);
        sensor.setDescription("some updated description");
        try {
            sensorService.update(sensor.getId(), sensor, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = sensorDao.findByName("test_sensor_54321", SensorDTO.class).get().getDescription();
            String actual = "some updated description";
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            sensorService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(sensorDao.existsByStaIdentifier(entityId, SensorDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(4)
    public void testDeleteWithRelatedDatastream() {
        entityId="501";
        try {
            sensorService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(sensorDao.existsByStaIdentifier(entityId, SensorDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(5)
    public void testGetEntity() {
        entityId="801";
        try {
            SensorDTO sensor = sensorService.getEntity(entityId, QUERY_OPTIONS_FACTORY.createQueryOptions(""));
            Assertions.assertNotNull(sensor);
            Assertions.assertEquals(sensor.getId(), entityId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(6)
    public void testGetEntityWithSelectOption() {
        entityId="801";
        try {
            SensorDTO sensor = sensorService.getEntity(
                    entityId,
                    QUERY_OPTIONS_FACTORY.createQueryOptions("$select=id")
            );
            Assertions.assertNotNull(sensor);
            Assertions.assertEquals(sensor.getId(), entityId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(6)
    public void testGetEntityWithExpandDatastream() {
        entityId="1";
        try {
            SensorDTO sensor = sensorService.getEntity(
                    entityId,
                    QUERY_OPTIONS_FACTORY.createQueryOptions("$expand=Datastreams($select=id, name, description)")
            );
            Assertions.assertNotNull(sensor);
            Assertions.assertEquals(sensor.getId(), entityId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(7)
    public void testGetEntityCollectionWithCountFilter() {
        try {
            CollectionWrapper collection = sensorService.getEntityCollection(
                    QUERY_OPTIONS_FACTORY.createQueryOptions("$count=true")
            );
            List<SensorDTO> entities = (List<SensorDTO>) collection.getEntities();
            Assertions.assertNotNull(entities);
            Assertions.assertFalse(entities.isEmpty());
            Assertions.assertEquals(collection.getTotalEntityCount(), 505);
        } catch (Exception e) {
            fail();
        }
    }
}

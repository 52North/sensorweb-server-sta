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
import org.n52.shetland.ogc.om.OmConstants;
import org.n52.sta.api.dto.*;
import org.n52.sta.api.dto.impl.*;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatastreamServiceTest {
    private final DSLContext ctx;
    private final FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose;
    private final MutexFactory mutex;
    private final ObservationQueryConditions oQC;
    private final DatastreamQueryConditions dQC;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;
    private final ThingDaoImpl thingDao;
    private final SensorDaoImpl sensorDao;
    private final ThingLocationDaoImpl thingLocationDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final LocationDaoImpl locationDao;
    private final ObservationDaoImpl observationDao;
    private final DatastreamDaoImpl datastreamDao;
    private final FeatureOfInterestDaoImpl featureDao;
    private final HistoricalLocationDaoImpl historicalLocationDao;
    private final FormatDaoImpl formatDao;
    private final ObservedPropertyDaoImpl observedPropertyDao;
    private final UnitDaoImpl unitDao;
    private final CloudNativeObservedPropertyService observedPropertyService;
    private final CloudNativeThingService thingService;
    private final CloudNativeObservationService observationService;
    private final CloudNativeLocationService locationService;
    private final CloudNativeHistoricalLocationService historicalLocationService;
    private final CloudNativeFeatureOfInterestService featureService;
    private final CloudNativeFormatService formatService;
    private final CloudNativeDatastreamService datastreamService;
    private final CloudNativeSensorService sensorService;
    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public DatastreamServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);
        this.mutex = new MutexFactory();

        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose);
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        unitDao = new UnitDaoImpl(ctx, staFirehose);
        observationDao = new ObservationDaoImpl(ctx, staFirehose);

        formatService = new CloudNativeFormatService(mutex, formatDao);
        datastreamService = new CloudNativeDatastreamService(datastreamDao,
                formatService,
                observationDao,
                unitDao,
                mutex);

        // dependencies
        locationDao = new LocationDaoImpl(ctx, staFirehose);
        sensorDao = new SensorDaoImpl(ctx, staFirehose);
        featureDao = new FeatureOfInterestDaoImpl(ctx, staFirehose);
        thingDao = new ThingDaoImpl(ctx, staFirehose);
        observedPropertyDao = new ObservedPropertyDaoImpl(ctx, staFirehose);
        thingLocationDao = new ThingLocationDaoImpl(staFirehose, ctx);
        locationHistoricalLocationDao = new LocationHistoricalLocationDaoImpl(staFirehose, ctx);
        historicalLocationDao = new HistoricalLocationDaoImpl(ctx, staFirehose);
        featureService = new CloudNativeFeatureOfInterestService(formatService,
                observationDao,
                datastreamDao,
                featureDao,
                mutex);
        observationService = new CloudNativeObservationService(observationDao,
                datastreamDao,
                locationDao,
                mutex);
        sensorService = new CloudNativeSensorService(sensorDao,
                datastreamDao,
                formatService,
                mutex);
        observedPropertyService = new CloudNativeObservedPropertyService(observedPropertyDao,
                datastreamDao,
                mutex);
        thingService = new CloudNativeThingService(thingDao,
                thingLocationDao,
                locationHistoricalLocationDao,
                datastreamDao,
                mutex);
        locationService = new CloudNativeLocationService(locationDao,
                formatService,
                locationHistoricalLocationDao,
                thingLocationDao,
                false,
                mutex);
        historicalLocationService = new CloudNativeHistoricalLocationService(
                historicalLocationDao,
                locationHistoricalLocationDao,
                mutex);
        oQC = new ObservationQueryConditions();
        oQC.setDslContext(ctx);
        dQC = new DatastreamQueryConditions();
        dQC.setDslContext(ctx);
        CloudNativeObservationService.setObservationQueryConditions(oQC);
        CloudNativeDatastreamService.setDatastreamQueryConditions(dQC);

        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Thing))
                .thenReturn(thingService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Sensor))
                .thenReturn(sensorService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.ObservedProperty))
                .thenReturn(observedPropertyService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Observation))
                .thenReturn(observationService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream))
                .thenReturn(datastreamService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.FeatureOfInterest))
                .thenReturn(featureService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Location))
                .thenReturn(locationService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.HistoricalLocation))
                .thenReturn(historicalLocationService);
        datastreamService.setServiceRepository(mockServiceRepository);
        observationService.setServiceRepository(mockServiceRepository);
        sensorService.setServiceRepository(mockServiceRepository);
        observedPropertyService.setServiceRepository(mockServiceRepository);
        thingService.setServiceRepository(mockServiceRepository);
    }

    @Test
    @Order(1)
    public void testCreate() {
        DatastreamDTO datastream = new Datastream();
        datastream.setId("12345");
        datastream.setName("Sample Datastream");
        datastream.setDescription("Sample description");
        DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
        uom.setDefinition("Pressure");
        uom.setSymbol("P");
        uom.setName("Pressure");
        datastream.setUnitOfMeasurement(uom);
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("ds_code", "ds_dummy");
        datastream.setProperties(properties);
        datastream.setObservationType("composite");

        // existing Sensor
        SensorDTO sensor = new Sensor();
        sensor.setId("500");
        datastream.setSensor(sensor);

        // existing Thing
        ThingDTO thing = new Thing();
        thing.setId("2");
        datastream.setThing(thing);

        // existing ObservedProperty
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setId("1");
        datastream.setObservedProperty(observedProperty);

        try {
            entityId = datastreamService.create(datastream).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    @Order(2)
    public void testUpdate() {
        DatastreamDTO datastream = new Datastream();
        entityId = "1727679230768000";
        datastream.setId(entityId);
        datastream.setDescription("Updated sample description");
        try {
            datastreamService.update(entityId, datastream, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = "Updated sample description";
            String actual = datastreamDao
                    .findById(Long.parseLong(entityId), null, DatastreamDTO.class)
                    .get()
                    .getDescription();
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            entityId = "1727725519082000";
            datastreamService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(4)
    public void testCreateWithLinkedEntities_Sensor() {
        DatastreamDTO datastream = new Datastream();
        datastream.setId("12345");
        datastream.setName("Sample Datastream");
        datastream.setDescription("Sample description");
        DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
        uom.setDefinition("Pressure");
        uom.setSymbol("P");
        uom.setName("Pressure");
        datastream.setUnitOfMeasurement(uom);
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("ds_with_Sensor_code", "ds_dummy");
        datastream.setProperties(properties);
        datastream.setObservationType("composite");

        // new Sensor
        SensorDTO sensor = new Sensor();
        sensor.setId("2024");
        sensor.setName("Datastream_Sensor");
        sensor.setDescription("Sensor created along with Datastream");
        sensor.setMetadata("random.metadata.com/datastream_sensor");
        sensor.setEncodingType("application/pdf");
        datastream.setSensor(sensor);

        // existing Thing
        ThingDTO thing = new Thing();
        thing.setId("2");
        datastream.setThing(thing);

        // existing ObservedProperty
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setId("1");
        datastream.setObservedProperty(observedProperty);

        try {
            entityId = datastreamService.create(datastream).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(5)
    public void testCreateWithLinkedEntities_ObservedProperty() {
        DatastreamDTO datastream = new Datastream();
        datastream.setId("12345");
        datastream.setName("Sample Datastream");
        datastream.setDescription("Sample description");
        DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
        uom.setDefinition("Pressure");
        uom.setSymbol("P");
        uom.setName("Pressure");
        datastream.setUnitOfMeasurement(uom);
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("ds_with_observedProperty_code", "ds_dummy");
        datastream.setProperties(properties);
        datastream.setObservationType("composite");

        // existing Sensor
        SensorDTO sensor = new Sensor();
        sensor.setId("500");
        datastream.setSensor(sensor);

        // existing Thing
        ThingDTO thing = new Thing();
        thing.setId("2");
        datastream.setThing(thing);

        // new ObservedProperty
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setId("2024");
        observedProperty.setName("Datastream_ObservedProperty");
        observedProperty.setDescription("Observed property created along with Datastream");
        observedProperty.setDefinition("http://sweet.jpl.nasa.gov/ontology/property.owl#CO");
        datastream.setObservedProperty(observedProperty);

        try {
            entityId = datastreamService.create(datastream).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(5)
    public void testCreateWithLinkedEntities_Thing() {
        DatastreamDTO datastream = new Datastream();
        datastream.setId("12345");
        datastream.setName("Test New Datastream");
        datastream.setDescription("Test New description");
        DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
        uom.setDefinition("Pressure");
        uom.setSymbol("P");
        uom.setName("Pressure");
        datastream.setUnitOfMeasurement(uom);
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("ds_with_Thing_code", "ds_test_dummy");
        datastream.setProperties(properties);
        datastream.setObservationType(OmConstants.OBS_TYPE_MEASUREMENT);

        // existing Sensor
        SensorDTO sensor = new Sensor();
        sensor.setId("501");
        datastream.setSensor(sensor);

        // new Thing
        ThingDTO thing = new Thing();
        thing.setId("2024");
        thing.setName("Test_Datastream_Thing");
        thing.setDescription("Test Thing created along with Datastream");
        LocationDTO location = new Location();
        location.setId("2025");
        location.setName("Test Location with Thing & Datastream");
        location.setDescription("Test Location created along with Thing & Datastream");
        try {
            String wktLineString = "LINESTRING (30 10, 40 40)";
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            Assert.fail();
        }
        thing.setLocations(Collections.singleton(location));
        datastream.setThing(thing);

        // existing ObservedProperty
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setId("1");
        datastream.setObservedProperty(observedProperty);

        try {
            entityId = datastreamService.create(datastream).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(5)
    public void testCreateWithLinkedEntities_All() {
        DatastreamDTO datastream = new Datastream();
        datastream.setId("12345");
        datastream.setName("Sample Datastream");
        datastream.setDescription("Sample description");
        DatastreamDTO.UnitOfMeasurement uom = new DatastreamDTO.UnitOfMeasurement();
        uom.setDefinition("Volume");
        uom.setSymbol("V");
        uom.setName("Volume");
        datastream.setUnitOfMeasurement(uom);
        datastream.setResultTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        datastream.setPhenomenonTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        ObjectNode properties = mapper.createObjectNode();
        properties.put("ds_with_all_code", "ds_all_dummy");
        datastream.setProperties(properties);
        datastream.setObservationType("composite");

        // new Sensor
        SensorDTO sensor = new Sensor();
        sensor.setId("2024");
        sensor.setName("Datastream_Sensor_Batch");
        sensor.setDescription("Sensor created along with Datastream");
        sensor.setMetadata("random.metadata.com/datastream_sensor_batch");
        sensor.setEncodingType("application/pdf");
        datastream.setSensor(sensor);

        // new Thing
        ThingDTO thing = new Thing();
        thing.setId("2024");
        thing.setName("Datastream_Thing_Batch");
        thing.setDescription("Thing created along with Datastream");
        LocationDTO location = new Location();
        location.setId("2025");
        location.setName("Location_Thing_Datastream_Batch");
        location.setDescription("Location created along with Thing & Datastream");
        try {
            String wktLineString = "LINESTRING (30 10, 40 40)";
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            Assert.fail();
        }
        thing.setLocations(Collections.singleton(location));
        datastream.setThing(thing);

        // new ObservedProperty
        ObservedPropertyDTO observedProperty = new ObservedProperty();
        observedProperty.setId("2024");
        observedProperty.setName("Datastream_ObservedProperty_Batch");
        observedProperty.setDescription("Observed property created along with Datastream (Batch)");
        observedProperty.setDefinition("http://sweet.jpl.nasa.gov/ontology/property.owl#Batch");
        datastream.setObservedProperty(observedProperty);

        try {
            entityId = datastreamService.create(datastream).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(datastreamDao.existsByStaIdentifier(entityId, DatastreamDTO.class));
        } catch (Exception e) {
            fail();
        }
    }
}

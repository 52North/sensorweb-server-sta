package org.n52.sta.data.cloudnative.test.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.api.dto.impl.HistoricalLocation;
import org.n52.sta.api.dto.impl.Location;
import org.n52.sta.api.dto.impl.Thing;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.EntityQueryConditions;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocationServiceTest {
    private final DSLContext ctx;
    private final LocationDaoImpl locationDao;
    private final FormatDaoImpl formatDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final ThingLocationDaoImpl thingLocationDao;
    private final ThingDaoImpl thingDao;
    private final HistoricalLocationDaoImpl historicalLocationDao;
    private final CloudNativeThingService thingService;
    private final CloudNativeFormatService formatService;
    private final CloudNativeHistoricalLocationService historicalLocationService;
    private final MutexFactory mutex;
    private final CloudNativeLocationService locationService;
    boolean updateFOIFeatureEnabled;
    private final FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;
    private final LocationQueryConditions lQC;
    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public LocationServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);
        updateFOIFeatureEnabled = true;
        mutex = new MutexFactory();
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        formatService = new CloudNativeFormatService(mutex, formatDao);
        locationDao = new LocationDaoImpl(ctx, staFirehose);
        locationHistoricalLocationDao = new LocationHistoricalLocationDaoImpl(staFirehose, ctx);
        thingLocationDao = new ThingLocationDaoImpl(staFirehose, ctx);
        locationService = new CloudNativeLocationService(locationDao,
                formatService,
                locationHistoricalLocationDao,
                thingLocationDao,
                updateFOIFeatureEnabled,
                mutex);

        lQC = new LocationQueryConditions();
        lQC.setDslContext(ctx);
        CloudNativeLocationService.setLocationQueryConditions(lQC);

        // dependencies
        historicalLocationDao = new HistoricalLocationDaoImpl(ctx, staFirehose);
        thingDao = new ThingDaoImpl(ctx, staFirehose);
        thingService = new CloudNativeThingService(thingDao,
                thingLocationDao,
                locationHistoricalLocationDao,
                new DatastreamDaoImpl(ctx, staFirehose),
                mutex);
        historicalLocationService = new CloudNativeHistoricalLocationService(
                historicalLocationDao,
                locationHistoricalLocationDao,
                mutex);

    }

    @Test
    @Order(1)
    public void testCreate() {
        LocationDTO location = new Location();
        location.setId("12345");
        location.setName("Home");
        location.setDescription("Home address");
        String wktLineString = "LINESTRING (30 10, 10 30, 40 40)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        ObjectNode properties = mapper.createObjectNode();
        properties.put("loc_code", "home_007_pt");
        location.setProperties(properties);
        try {
            entityId = locationService.create(location).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(locationDao.existsByName("Home", LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(2)
    public void testUpdate() {
        LocationDTO location = new Location();
        location.setId(entityId);
        location.setName("Updated home");
        location.setDescription("Updated home address");
        try {
            locationService.update(entityId, location, HttpMethod.PATCH);
            Thread.sleep(45000);
            Assertions.assertTrue(locationDao.existsByName("Updated home", LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            locationService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(locationDao.existsByStaIdentifier(entityId, LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(4)
    public void testCreateWithThing() {

        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Thing))
                .thenReturn(thingService);
        locationService.setServiceRepository(mockServiceRepository);

        LocationDTO location = new Location();
        location.setId("12345");
        location.setName("New Location with Thing");
        location.setDescription("Test New Location with Nested Thing");
        String wktLineString = "LINESTRING (30 10, 10 30)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        ObjectNode properties = mapper.createObjectNode();
        properties.put("locThing_code", "locThing_007_pt");
        location.setProperties(properties);

        ThingDTO thing = new Thing();
        thing.setId("54321");
        thing.setDescription("nested thing sample");
        thing.setName("some new dummy thing");
        location.setThings(Collections.singleton(thing));
        try {
            entityId = locationService.create(location).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(locationDao.existsByName("New Location with Thing", LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(5)
    public void testDeleteWithThing() {
        try {
            locationService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(locationDao.existsByStaIdentifier(entityId, LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(6)
    public void testCreateWithThingAndHistoricalLocation() {
        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Thing))
                .thenReturn(thingService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.HistoricalLocation))
                .thenReturn(historicalLocationService);
        locationService.setServiceRepository(mockServiceRepository);
        thingService.setServiceRepository(mockServiceRepository);

        LocationDTO location = new Location();
        location.setId("12345");
        location.setName("Location with Thing & HistoricalLocation");
        location.setDescription("Test Location with Nested Thing & HistoricalLocation");
        String wktLineString = "LINESTRING (30 10, 10 30)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        ObjectNode properties = mapper.createObjectNode();
        properties.put("code", "Loc_Thing_HistLoc");
        location.setProperties(properties);

        ThingDTO thing = new Thing();
        thing.setId("54321");
        thing.setDescription("nested thing with historical location sample");
        thing.setName("some new dummy thing with related historical location");
        location.setThings(Collections.singleton(thing));

        HistoricalLocationDTO historicalLocation = new HistoricalLocation();
        historicalLocation.setTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));
        thing.setHistoricalLocations(Collections.singleton(historicalLocation));

        try {
            entityId = locationService.create(location).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(locationDao.existsByName("Location with Thing & HistoricalLocation",
                    LocationDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}

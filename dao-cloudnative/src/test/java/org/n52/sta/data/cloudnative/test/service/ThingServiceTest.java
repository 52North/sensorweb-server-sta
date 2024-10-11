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
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.api.dto.impl.Location;
import org.n52.sta.api.dto.impl.Thing;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.*;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThingServiceTest {
    private final DSLContext ctx;
    private final MutexFactory mutex;
    private final FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;

    private final ObservationQueryConditions oQC;
    private final DatastreamQueryConditions dsQC;
    private final FeatureOfInterestQueryConditions foiQC;
    private final ThingQueryConditions tQC;
    private final SensorQueryConditions sQC;
    private final ObservedPropertyQueryConditions opQC;
    private final HistoricalLocationQueryConditions hlQC;
    private final LocationQueryConditions lQC;

    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final ThingLocationDaoImpl thingLocationDao;
    private final ThingDaoImpl thingDao;
    // dependency dao
    private final FormatDaoImpl formatDao;
    private final LocationDaoImpl locationDao;
    private final HistoricalLocationDaoImpl historicalLocationDao;
    private final DatastreamDaoImpl datastreamDao;
    private final ObservationDaoImpl observationDao;
    private final UnitDaoImpl unitDao;

    private final CloudNativeThingService thingService;
    // dependency services
    private final CloudNativeDatastreamService datastreamService;
    private final CloudNativeHistoricalLocationService historicalLocationService;
    private final CloudNativeLocationService locationService;
    private final CloudNativeFormatService formatService;

    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ThingServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);
        this.mutex = new MutexFactory();

        oQC = new ObservationQueryConditions();
        dsQC = new DatastreamQueryConditions();
        foiQC = new FeatureOfInterestQueryConditions();
        tQC = new ThingQueryConditions();
        sQC = new SensorQueryConditions();
        opQC = new ObservedPropertyQueryConditions();
        lQC = new LocationQueryConditions();
        hlQC = new HistoricalLocationQueryConditions();
        sQC.setDslContext(ctx);
        opQC.setDslContext(ctx);
        foiQC.setDslContext(ctx);
        dsQC.setDslContext(ctx);
        oQC.setDslContext(ctx);
        tQC.setDslContext(ctx);
        lQC.setDslContext(ctx);
        hlQC.setDslContext(ctx);

        thingLocationDao = new ThingLocationDaoImpl(staFirehose, ctx);
        locationHistoricalLocationDao = new LocationHistoricalLocationDaoImpl(staFirehose, ctx);
        thingDao = new ThingDaoImpl(ctx, staFirehose, tQC, lQC, dsQC, hlQC);
        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose, dsQC, tQC, sQC, opQC);


        thingService = new CloudNativeThingService(thingDao,
                thingLocationDao,
                locationHistoricalLocationDao,
                datastreamDao,
                mutex,
                tQC,
                dsQC);

        // dependencies
        observationDao = new ObservationDaoImpl(ctx, staFirehose, dsQC, oQC);
        locationDao = new LocationDaoImpl(ctx, staFirehose, hlQC, lQC, tQC);
        historicalLocationDao = new HistoricalLocationDaoImpl(ctx, staFirehose, lQC, tQC, hlQC);
        unitDao = new UnitDaoImpl(ctx, staFirehose);
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        formatService = new CloudNativeFormatService(mutex, formatDao);
        locationService = new CloudNativeLocationService(locationDao,
                formatService,
                locationHistoricalLocationDao,
                thingLocationDao,
                false,
                mutex,
                lQC);
        historicalLocationService = new CloudNativeHistoricalLocationService(
                historicalLocationDao,
                locationHistoricalLocationDao,
                mutex,
                hlQC);
        datastreamService = new CloudNativeDatastreamService(datastreamDao,
                formatService,
                observationDao,
                unitDao,
                mutex,
                dsQC);
        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.HistoricalLocation))
                .thenReturn(historicalLocationService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Location))
                .thenReturn(locationService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream))
                .thenReturn(datastreamService);
        thingService.setServiceRepository(mockServiceRepository);
        historicalLocationService.setServiceRepository(mockServiceRepository);
    }

    @Test
    @Order(1)
    public void testCreate() {
        ThingDTO thing = new Thing();
        thing.setId("12345");
        thing.setName("MSI");
        thing.setDescription("This is a thing");
        ObjectNode properties = mapper.createObjectNode();
        properties.put("thing_code", "gf65_9sd");
        thing.setProperties(properties);
        try {
            entityId = thingService.create(thing).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(thingDao.existsByStaIdentifier(entityId, ThingDTO.class));
        }  catch (Exception e) {
            fail();

        }
    }

    @Test
    @Order(2)
    public void testUpdate() {
        ThingDTO thing = new Thing();
        thing.setId(entityId);
        thing.setName("MSI");
        thing.setDescription("This is an updated thing");
        try {
            thingService.update(entityId, thing, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = "This is an updated thing";
            String actual = thingDao.findByName("MSI", ThingDTO.class).get().getDescription();
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            thingService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(thingDao.existsByStaIdentifier(entityId, ThingDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(4)
    public void testCreateWithLocation() {
        ThingDTO thing = new Thing();
        thing.setId("12345");
        thing.setName("Acer");
        thing.setDescription("This is a thing");
        ObjectNode properties = mapper.createObjectNode();
        properties.put("thing_code", "aspire d257");
        thing.setProperties(properties);

        LocationDTO location = new Location();
        location.setId("12345");
        location.setName("Acer home");
        location.setDescription("Acer home address");
        String wktLineString = "LINESTRING (30 10, 10 30, 40 40)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            Assert.fail();
        }
        ObjectNode loc_properties = mapper.createObjectNode();
        loc_properties.put("loc_code", "home_007_pt");
        location.setProperties(loc_properties);

        thing.setLocations(Collections.singleton(location));

        try {
            entityId = thingService.create(thing).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(thingDao.existsByStaIdentifier(entityId, ThingDTO.class));
            // Assertions.assertTrue(historicalLocationDao.find);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(4)
    public void updateWithLocation() {
        entityId = "1727725674857000";
        ThingDTO thing = new Thing();
        thing.setId(entityId);
        thing.setName("Datastream_Thing_Updated");
        thing.setDescription("This is an updated thing created along with Datastream");
        try {
            thingService.update(entityId, thing, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = "This is an updated thing";
            String actual = thingDao.findByName("Datastream_Thing_Updated", ThingDTO.class).get().getDescription();
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    @Order(5)
    public void testDeleteWithLocation() {
        try {
            thingService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(thingDao.existsByStaIdentifier(entityId, ThingDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(6)
    public void testDeleteWithLocationDatastreamObservation() {
        try {
            entityId = "1727581856727000";
            thingService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(thingDao.existsByStaIdentifier(entityId, ThingDTO.class));
        } catch (Exception e) {
            fail();
        }
    }
}

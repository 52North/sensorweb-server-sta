package org.n52.sta.data.cloudnative.test.service;

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
import org.n52.sta.data.cloudnative.condition.*;
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

import org.n52.shetland.ogc.gml.time.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HistoricalLocationServiceTest {
    private final DSLContext ctx;
    private final MutexFactory mutex;
    private final FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose;
    private final LocationDaoImpl locationDao;
    private final HistoricalLocationDaoImpl historicalLocationDao;
    private final FormatDaoImpl formatDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final ThingLocationDaoImpl thingLocationDao;
    private final ThingDaoImpl thingDao;
    private final DatastreamDaoImpl datastreamDao;
    private final CloudNativeHistoricalLocationService historicalLocationService;
    private final CloudNativeLocationService locationService;
    private final CloudNativeFormatService formatService;
    private final CloudNativeThingService thingService;
    private static String entityId;

    private final DatastreamQueryConditions dsQC;
    private final ThingQueryConditions tQC;
    private final SensorQueryConditions sQC;
    private final ObservedPropertyQueryConditions opQC;
    private final HistoricalLocationQueryConditions hlQC;
    private final LocationQueryConditions lQC;

    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public HistoricalLocationServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);
        this.mutex = new MutexFactory();

        dsQC = new DatastreamQueryConditions();
        tQC = new ThingQueryConditions();
        sQC = new SensorQueryConditions();
        opQC = new ObservedPropertyQueryConditions();
        lQC = new LocationQueryConditions();
        hlQC = new HistoricalLocationQueryConditions();
        sQC.setDslContext(ctx);
        opQC.setDslContext(ctx);
        dsQC.setDslContext(ctx);
        tQC.setDslContext(ctx);
        lQC.setDslContext(ctx);
        hlQC.setDslContext(ctx);

        locationDao = new LocationDaoImpl(ctx, staFirehose, hlQC, lQC, tQC);
        historicalLocationDao = new HistoricalLocationDaoImpl(ctx, staFirehose, lQC, tQC, hlQC);
        locationHistoricalLocationDao = new LocationHistoricalLocationDaoImpl(staFirehose, ctx);
        historicalLocationService = new CloudNativeHistoricalLocationService(
                historicalLocationDao,
                locationHistoricalLocationDao,
                mutex,
                hlQC);

        // dependencies
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        formatService = new CloudNativeFormatService(mutex, formatDao);
        thingLocationDao = new ThingLocationDaoImpl(staFirehose, ctx);
        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose, dsQC, tQC, sQC, opQC);
        locationService = new CloudNativeLocationService(locationDao,
                formatService,
                locationHistoricalLocationDao,
                thingLocationDao,
                false,
                mutex,
                lQC);
        thingDao = new ThingDaoImpl(ctx, staFirehose, tQC, lQC, dsQC, hlQC);
        thingService = new CloudNativeThingService(thingDao,
                thingLocationDao,
                locationHistoricalLocationDao,
                datastreamDao,
                mutex,
                tQC,
                dsQC);

    }

    @Test
    @Order(1)
    public void testWithCreate() {

        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Thing))
                .thenReturn(thingService);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Location))
                .thenReturn(locationService);
        historicalLocationService.setServiceRepository(mockServiceRepository);

        HistoricalLocationDTO historicalLocation = new HistoricalLocation();
        historicalLocation.setId("12345");
        historicalLocation.setTime(TimeUtil.createTime(TimeUtil.createDateTime(
                Timestamp.from(Instant.now())
        )));

        LocationDTO location = new Location();
        location.setId("12345");
        location.setName("Home");
        location.setDescription("Home address");
        String wktLineString = "LINESTRING (30 10, 10 30, 40 40)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            location.setGeometry(geom);
        } catch (Exception e) {
            fail();
        }
        historicalLocation.setLocations(Collections.singleton(location));

        ThingDTO thing = new Thing();
        thing.setId("2");
        historicalLocation.setThing(thing);
        try {
            entityId = historicalLocationService.create(historicalLocation).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(historicalLocationService.existsEntity(entityId));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(2)
    public void testUpdate() {
        HistoricalLocationDTO historicalLocation = new HistoricalLocation();
        historicalLocation.setId(entityId);
        Time now = TimeUtil.createTime(TimeUtil.createDateTime(Timestamp.from(Instant.now())));
        historicalLocation.setTime(now);
        try {
            historicalLocationService.update(entityId, historicalLocation, HttpMethod.PATCH);
            Thread.sleep(45000);
            Assertions.assertEquals(historicalLocationDao
                    .findById(Long.parseLong(entityId), null, HistoricalLocationDTO.class)
                    .get().getTime(), now);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            entityId = "1727567885954000";
            historicalLocationService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(historicalLocationDao.existsByStaIdentifier(entityId, HistoricalLocationDTO.class));
        } catch(Exception e) {
            fail();
        }
    }
}

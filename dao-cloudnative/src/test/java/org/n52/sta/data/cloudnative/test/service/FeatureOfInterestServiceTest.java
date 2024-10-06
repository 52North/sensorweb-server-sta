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
import org.n52.sta.api.dto.FeatureOfInterestDTO;
import org.n52.sta.api.dto.impl.FeatureOfInterest;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.FeatureOfInterestQueryConditions;
import org.n52.sta.data.cloudnative.dao.impl.*;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.service.CloudNativeEntityServiceRepository;
import org.n52.sta.data.cloudnative.service.CloudNativeFeatureOfInterestService;
import org.n52.sta.data.cloudnative.service.CloudNativeFormatService;
import org.n52.sta.data.cloudnative.service.CloudNativeObservationService;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeatureOfInterestServiceTest {
    private DSLContext ctx;
    private FirehoseClient firehoseClient;
    private StaFirehoseClient staFirehose;

    private DatastreamDaoImpl datastreamDao;
    private FormatDaoImpl formatDao;
    private ObservationDaoImpl observationDao;
    private FeatureOfInterestDaoImpl featureDao;
    private LocationDaoImpl locationDao;

    private FeatureOfInterestQueryConditions foiQC;
    private DatastreamQueryConditions dsQC;
    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    private CloudNativeFeatureOfInterestService foiService;
    private CloudNativeObservationService observationService;
    private CloudNativeFormatService formatService;

    private static String entityId;
    private final MutexFactory mutex = new MutexFactory();
    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public FeatureOfInterestServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        MockitoAnnotations.openMocks(this);
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);

        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose);
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        observationDao = new ObservationDaoImpl(ctx, staFirehose);
        featureDao = new FeatureOfInterestDaoImpl(ctx, staFirehose);
        locationDao = new LocationDaoImpl(ctx, staFirehose);

        formatService = new CloudNativeFormatService(mutex, formatDao);
        foiService = new CloudNativeFeatureOfInterestService(
                formatService,
                observationDao,
                datastreamDao,
                featureDao,
                mutex);
        observationService = new CloudNativeObservationService(
                observationDao,
                datastreamDao,
                locationDao,
                mutex
        );

        foiQC = new FeatureOfInterestQueryConditions();
        dsQC = new DatastreamQueryConditions();
        foiQC.setDslContext(ctx);
        dsQC.setDslContext(ctx);
        CloudNativeFeatureOfInterestService.setFeatureQueryConditions(foiQC);
        CloudNativeFeatureOfInterestService.setDatastreamQueryConditions(dsQC);
    }

    @Test
    @Order(1)
    public void testCreate() {
        // Stub a method on the mock repository
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Observation))
                .thenReturn(observationService);
        foiService.setServiceRepository(mockServiceRepository);

        FeatureOfInterestDTO foi = new FeatureOfInterest();
        foi.setId("12345");
        foi.setName("Well #7");
        foi.setDescription("Gas Production Wellpad");
        ObjectNode properties = mapper.createObjectNode();
        properties.put("foi_code", "ogp_7");
        foi.setProperties(properties);
        String wktLineString = "LINESTRING (30 10, 10 30, 40 40)";
        try {
            Geometry geom = new WKTReader().read(wktLineString);
            foi.setFeature(geom);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        foi.setEncodingType("application/pdf");

        try {
            entityId = foiService.create(foi).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(featureDao.existsByStaIdentifier(entityId, FeatureOfInterestDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(2)
    public void testUpdate() {
        FeatureOfInterestDTO foi = new FeatureOfInterest();
        foi.setId(entityId);
        foi.setDescription("Updated Gas Production Wellpad");
        try {
            foiService.update(entityId, foi, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = featureDao.findById(
                    Long.parseLong(entityId), null, FeatureOfInterestDTO.class).get().getDescription();
            String actual = "Updated Gas Production Wellpad";
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            foiService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(featureDao.existsByStaIdentifier(entityId, FeatureOfInterestDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @Order(4)
    public void testDeleteWithDatastream() {
        try {
            entityId="2";
            foiService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(featureDao.existsByStaIdentifier(entityId, FeatureOfInterestDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}

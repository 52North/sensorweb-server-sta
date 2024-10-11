package org.n52.sta.data.cloudnative.test.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.sta.api.dto.ObservedPropertyDTO;
import org.n52.sta.api.dto.impl.ObservedProperty;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.*;
import org.n52.sta.data.cloudnative.dao.impl.*;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.service.CloudNativeDatastreamService;
import org.n52.sta.data.cloudnative.service.CloudNativeEntityServiceRepository;
import org.n52.sta.data.cloudnative.service.CloudNativeFormatService;
import org.n52.sta.data.cloudnative.service.CloudNativeObservedPropertyService;
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
public class ObservedPropertyServiceTest {
    private DSLContext ctx;
    private FirehoseClient firehoseClient;
    private DatastreamDaoImpl datastreamDao;
    private StaFirehoseClient staFirehose;
    private final MutexFactory mutex = new MutexFactory();
    private ObservedPropertyDaoImpl observedPropertyDao;
    private CloudNativeObservedPropertyService observedPropertyService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String entityId;
    private final ObservationDaoImpl observationDao;
    private final FormatDaoImpl formatDao;
    private final UnitDaoImpl unitDao;
    private final CloudNativeFormatService formatService;
    private final CloudNativeDatastreamService datastreamService;

    private final ObservationQueryConditions oQC;
    private final DatastreamQueryConditions dsQC;
    private final FeatureOfInterestQueryConditions foiQC;
    private final ThingQueryConditions tQC;
    private final SensorQueryConditions sQC;
    private final ObservedPropertyQueryConditions opQC;
    private final HistoricalLocationQueryConditions hlQC;
    private final LocationQueryConditions lQC;

    @Mock
    private CloudNativeEntityServiceRepository mockServiceRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ObservedPropertyServiceTest(DSLContext ctx, FirehoseClient firehoseClient) {
        this.ctx = ctx;
        this.firehoseClient = firehoseClient;
        this.staFirehose = new StaFirehoseClient(firehoseClient);

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

        datastreamDao = new DatastreamDaoImpl(ctx, staFirehose, dsQC, tQC, sQC, opQC);
        observedPropertyDao = new ObservedPropertyDaoImpl(ctx, staFirehose, dsQC, opQC);

        observedPropertyService = new CloudNativeObservedPropertyService(observedPropertyDao,
                datastreamDao,
                mutex,
                dsQC,
                opQC);


        observationDao = new ObservationDaoImpl(ctx, staFirehose, dsQC, oQC);
        formatDao = new FormatDaoImpl(ctx, staFirehose);
        unitDao = new UnitDaoImpl(ctx, staFirehose);
        formatService = new CloudNativeFormatService(mutex, formatDao);
        datastreamService = new CloudNativeDatastreamService(datastreamDao,
                formatService,
                observationDao,
                unitDao,
                mutex,
                dsQC);
        MockitoAnnotations.openMocks(this);
        when(mockServiceRepository.getEntityServiceRaw(CloudNativeEntityServiceRepository.EntityTypes.Datastream))
                .thenReturn(datastreamService);
        observedPropertyService.setServiceRepository(mockServiceRepository);
    }

    @Test
    @Order(1)
    public void testCreate() {
        ObservedPropertyDTO observedPropertyDTO = new ObservedProperty();
        observedPropertyDTO.setName("Carbon Monoxide CO");
        observedPropertyDTO.setDescription("description");
        observedPropertyDTO.setDefinition("http://sweet.jpl.nasa.gov/ontology/property.owl#CO");
        observedPropertyDTO.setId(null);
        ObjectNode properties = mapper.createObjectNode();
        properties.put("cc_code", "CO");
        observedPropertyDTO.setProperties(properties);
        try {
            entityId = observedPropertyService.create(observedPropertyDTO).getId();
            Thread.sleep(45000);
            Assertions.assertTrue(observedPropertyDao
                    .existsByName("Carbon Monoxide CO", ObservedPropertyDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    @Order(2)
    public void testUpdate() {
        ObservedPropertyDTO observedPropertyDTO = new ObservedProperty();
        observedPropertyDTO.setId(entityId);
        observedPropertyDTO.setDescription("updated description");
        try {
            observedPropertyService.update(entityId, observedPropertyDTO, HttpMethod.PATCH);
            Thread.sleep(45000);
            String expected = observedPropertyDao
                    .findByStaIdentifier(entityId, null, ObservedPropertyDTO.class).get().getDescription();
            String actual = "updated description";
            Assertions.assertEquals(expected, actual);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(3)
    public void testDelete() {
        try {
            observedPropertyService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(observedPropertyDao.existsByStaIdentifier(entityId, ObservedPropertyDTO.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(4)
    public void testDeleteWithDatastream() {
        try {
            entityId = "1727723082575000";
            observedPropertyService.delete(entityId);
            Thread.sleep(45000);
            Assertions.assertFalse(observedPropertyDao.existsByStaIdentifier(entityId, ObservedPropertyDTO.class));
        } catch (Exception e) {
            fail();
        }
    }
}

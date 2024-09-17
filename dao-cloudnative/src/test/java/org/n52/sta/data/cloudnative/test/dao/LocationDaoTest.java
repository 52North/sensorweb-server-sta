package org.n52.sta.data.cloudnative.test.dao;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.impl.LocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.util.StaFirehoseClient;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Location;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.firehose.FirehoseClient;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class LocationDaoTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private LocationDaoImpl locationDao;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private FirehoseClient firehoseClient;
    private final StaFirehoseClient staFirehose = new StaFirehoseClient(firehoseClient);

    @BeforeEach
    public void setUp() {
        locationDao = new LocationDaoImpl(ctx, staFirehose);
    }

    @Test
    public void testWithFindAllByThingId() {
        Long Id = 2L;
        Class<LocationDTO> entityClass = LocationDTO.class;
        List<LocationDTO> result = List.of();
        try {
            result = locationDao.findAllByThingId(Id, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getId(), "2");
    }

    @Test
    public void testWithExistsByName() {
        String name = "CCIT";
        Class<LocationDTO> entityClass = LocationDTO.class;
        boolean result = false;
        try {
            result = locationDao.existsByName(name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result);
    }

    @Test
    public void testWithFindByName() {
        String name = "CCIT";
        Class<LocationDTO> entityClass = LocationDTO.class;
        Optional<LocationDTO> result = Optional.empty();
        try {
            result = locationDao.findByName(name, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getName(), "CCIT");
    }

    @Test
    public void testWithGetColumn() {
        String name = "sta_identifier";
        String value = "2";
        Condition condition = DSL.field(name).eq(value);
        Class<LocationDTO> entityClass = LocationDTO.class;
        Optional<String> result = Optional.empty();
        try {
            result = locationDao.getColumn(condition, name, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get(), value);
    }

    @Test
    public void testWithFindById() {
        Long id = 2L;
        String description = "Calgary Centre for Innovative Technologies";
        QueryOptions options = null;
        Class<LocationDTO> entityClass = LocationDTO.class;
        Optional<LocationDTO> result = Optional.empty();
        try {
            result = locationDao.findById(id, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindOne() {
        String description = "Calgary Centre for Innovative Technologies";
        Condition predicate = StaEntity.LOCATION.DESCRIPTION.eq(description);
        QueryOptions options = null;
        Class<LocationDTO> entityClass = LocationDTO.class;
        Optional<LocationDTO> result = Optional.empty();
        try {
            result = locationDao.findOne(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getDescription(), description);
    }

    @Test
    public void testWithFindByStaIdentifier() {
        String identifier = "2";
        QueryOptions options = null;
        Class<LocationDTO> entityClass = LocationDTO.class;
        Optional<LocationDTO> result = Optional.empty();
        try {
            result = locationDao.findByStaIdentifier(identifier, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result.get().getId(), identifier);
    }

    @Test
    public void testWithFindAll() {
        Condition predicate = StaEntity.LOCATION_PROPERTIES.NAME.eq("loc_code");
        QueryOptions options = null;
        Class<LocationDTO> entityClass = LocationDTO.class;
        List<LocationDTO> result = List.of();
        try {
            result = locationDao.findAll(predicate, options, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(result.get(0).getProperties().findValue("loc_code").toString(),
                "\"lc.001.sample\"");
    }

    @Test
    public void testWithExistsByStaIdentifier() {
        String identifier = "2";
        Class<LocationDTO> entityClass = LocationDTO.class;
        boolean result = false;
        try {
            result = locationDao.existsByStaIdentifier(identifier, entityClass);
        } catch (STAInvalidQueryException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(result, true);
    }
}

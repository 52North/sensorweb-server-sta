package org.n52.sta;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.serialization.DatastreamSerdes.DatastreamDeserializer;
import org.n52.sta.data.serialization.DatastreamSerdes.DatastreamSerializer;
import org.n52.sta.data.serialization.FeatureOfInterestSerdes.FeatureOfInterestDeserializer;
import org.n52.sta.data.serialization.FeatureOfInterestSerdes.FeatureOfInterestSerializer;
import org.n52.sta.data.serialization.HistoricalLocationSerde.HistoricalLocationDeserializer;
import org.n52.sta.data.serialization.HistoricalLocationSerde.HistoricalLocationSerializer;
import org.n52.sta.data.serialization.LocationSerdes.LocationDeserializer;
import org.n52.sta.data.serialization.LocationSerdes.LocationSerializer;
import org.n52.sta.data.serialization.ObservationSerde.ObservationDeserializer;
import org.n52.sta.data.serialization.ObservationSerde.ObservationSerializer;
import org.n52.sta.data.serialization.ObservedPropertySerde.ObservedPropertyDeserializer;
import org.n52.sta.data.serialization.ObservedPropertySerde.ObservedPropertySerializer;
import org.n52.sta.data.serialization.SensorSerdes;
import org.n52.sta.data.serialization.SensorSerdes.SensorDeserializer;
import org.n52.sta.data.serialization.ThingSerdes.ThingDeserializer;
import org.n52.sta.data.serialization.ThingSerdes.ThingSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper customMapper(@Value("${server.rootUrl}") String rootUrl) {
        ArrayList<Module> modules = new ArrayList<>();

        //CollectionType Serialization
        SimpleModule module = new SimpleModule();

        // Register Serializers/Deserializers for all custom types
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new ThingSerializer(rootUrl));
        serializers.addSerializer(new LocationSerializer(rootUrl));
        serializers.addSerializer(new SensorSerdes.SensorSerializer(rootUrl));
        serializers.addSerializer(new ObservationSerializer(rootUrl));
        serializers.addSerializer(new ObservedPropertySerializer(rootUrl));
        serializers.addSerializer(new FeatureOfInterestSerializer(rootUrl));
        serializers.addSerializer(new HistoricalLocationSerializer(rootUrl));
        serializers.addSerializer(new DatastreamSerializer(rootUrl));

        SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(PlatformEntity.class, new ThingDeserializer());
        deserializers.addDeserializer(LocationEntity.class, new LocationDeserializer());
        deserializers.addDeserializer(ProcedureEntity.class, new SensorDeserializer());
        deserializers.addDeserializer(DataEntity.class, new ObservationDeserializer());
        deserializers.addDeserializer(PhenomenonEntity.class, new ObservedPropertyDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntity.class, new FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntity.class, new HistoricalLocationDeserializer());
        deserializers.addDeserializer(DatastreamEntity.class, new DatastreamDeserializer());

        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        modules.add(module);
        return Jackson2ObjectMapperBuilder.json()
                .modules(modules)
                .build();
    }
}

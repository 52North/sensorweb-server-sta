package org.n52.sta;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.SensorEntity;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamDeserializer;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamEntityPatch;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamPatchDeserializer;
import org.n52.sta.serdes.DatastreamSerdes.DatastreamSerializer;
import org.n52.sta.serdes.FeatureOfInterestSerdes.AbstractFeatureEntityPatch;
import org.n52.sta.serdes.FeatureOfInterestSerdes.FeatureOfInterestDeserializer;
import org.n52.sta.serdes.FeatureOfInterestSerdes.FeatureOfInterestPatchDeserializer;
import org.n52.sta.serdes.FeatureOfInterestSerdes.FeatureOfInterestSerializer;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationDeserializer;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationEntityPatch;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationPatchDeserializer;
import org.n52.sta.serdes.HistoricalLocationSerde.HistoricalLocationSerializer;
import org.n52.sta.serdes.LocationSerdes.LocationDeserializer;
import org.n52.sta.serdes.LocationSerdes.LocationEntityPatch;
import org.n52.sta.serdes.LocationSerdes.LocationPatchDeserializer;
import org.n52.sta.serdes.LocationSerdes.LocationSerializer;
import org.n52.sta.serdes.ObservationSerde.ObservationDeserializer;
import org.n52.sta.serdes.ObservationSerde.ObservationPatchDeserializer;
import org.n52.sta.serdes.ObservationSerde.ObservationSerializer;
import org.n52.sta.serdes.ObservationSerde.StaDataEntityPatch;
import org.n52.sta.serdes.ObservedPropertySerde.ObservedPropertyDeserializer;
import org.n52.sta.serdes.ObservedPropertySerde.ObservedPropertyPatchDeserializer;
import org.n52.sta.serdes.ObservedPropertySerde.ObservedPropertySerializer;
import org.n52.sta.serdes.ObservedPropertySerde.PhenomenonEntityPatch;
import org.n52.sta.serdes.SensorSerdes;
import org.n52.sta.serdes.SensorSerdes.SensorDeserializer;
import org.n52.sta.serdes.SensorSerdes.SensorEntityPatch;
import org.n52.sta.serdes.SensorSerdes.SensorPatchDeserializer;
import org.n52.sta.serdes.ThingSerdes.PlatformEntityPatch;
import org.n52.sta.serdes.ThingSerdes.ThingDeserializer;
import org.n52.sta.serdes.ThingSerdes.ThingPatchDeserializer;
import org.n52.sta.serdes.ThingSerdes.ThingSerializer;
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
        deserializers.addDeserializer(SensorEntity.class, new SensorDeserializer());
        deserializers.addDeserializer(DataEntity.class, new ObservationDeserializer());
        deserializers.addDeserializer(PhenomenonEntity.class, new ObservedPropertyDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntity.class, new FeatureOfInterestDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntity.class, new HistoricalLocationDeserializer());
        deserializers.addDeserializer(DatastreamEntity.class, new DatastreamDeserializer());

        deserializers.addDeserializer(PlatformEntityPatch.class, new ThingPatchDeserializer());
        deserializers.addDeserializer(LocationEntityPatch.class, new LocationPatchDeserializer());
        deserializers.addDeserializer(SensorEntityPatch.class, new SensorPatchDeserializer());
        deserializers.addDeserializer(StaDataEntityPatch.class, new ObservationPatchDeserializer());
        deserializers.addDeserializer(PhenomenonEntityPatch.class, new ObservedPropertyPatchDeserializer());
        deserializers.addDeserializer(AbstractFeatureEntityPatch.class, new FeatureOfInterestPatchDeserializer());
        deserializers.addDeserializer(HistoricalLocationEntityPatch.class, new HistoricalLocationPatchDeserializer());
        deserializers.addDeserializer(DatastreamEntityPatch.class, new DatastreamPatchDeserializer());


        module.setSerializers(serializers);
        module.setDeserializers(deserializers);
        modules.add(module);
        modules.add(new AfterburnerModule());
        return Jackson2ObjectMapperBuilder.json()
                .modules(modules)
                .build();
    }
}

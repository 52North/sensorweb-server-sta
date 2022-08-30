package org.n52.sta.data.editor;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.FeatureOfInterestData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.repositories.entity.FeatureOfInterestRepository;
import org.n52.sta.data.support.FeatureOfInterestGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FeatureOfInterestEntityEditor extends DatabaseEntityAdapter<AbstractFeatureEntity>
        implements
        EntityEditorDelegate<FeatureOfInterest, FeatureOfInterestData> {

    @Autowired
    private FeatureOfInterestRepository featureOfInterestRepository;

    @Autowired
    private ValueHelper valueHelper;

    private EntityEditorDelegate<Observation, ObservationData> observationEditor;

    public FeatureOfInterestEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.observationEditor = (EntityEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public FeatureOfInterestData getOrSave(FeatureOfInterest entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be set!");
        Optional<AbstractFeatureEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new FeatureOfInterestData(e, Optional.empty()))
                .orElseGet(() -> save(entity));
    }

    @Override
    public FeatureOfInterestData save(FeatureOfInterest entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be set!");

        String staIdentifier = entity.getId();
        EntityService<FeatureOfInterest> service = getService(FeatureOfInterest.class);

        if (service.exists(staIdentifier)) {
            throw new EditorException("FeatureOfInterest already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        FeatureEntity featureEntity = new FeatureEntity();
        featureEntity.setIdentifier(id);
        featureEntity.setStaIdentifier(id);
        featureEntity.setName(entity.getName());
        featureEntity.setDescription(entity.getDescription());
        featureEntity.setGeometry(entity.getFeature());

        valueHelper.setFormat(featureEntity::setFeatureType, entity.getEncodingType());

        //TODO: Autogenerate Feature based on Location
        //TODO: Implement updating of Geometry via 'updateFOI' Feature
        //TODO: evaluate if functionality of FeatureOfInterestService#alreadyExistsFeature is needed here
        //TODO: Implement persisting nested observations

        FeatureEntity saved = featureOfInterestRepository.save(featureEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
                .map(entry -> convertParameter(featureEntity, entry))
                .forEach(featureEntity::addParameter);

        // we need to flush else updates to relations are not persisted
        featureOfInterestRepository.flush();

        return new FeatureOfInterestData(saved, Optional.empty());
    }

    @Override
    public FeatureOfInterestData update(FeatureOfInterest entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<AbstractFeatureEntity> getEntity(String id) {
        FeatureOfInterestGraphBuilder graphBuilder = FeatureOfInterestGraphBuilder.createEmpty();
        return featureOfInterestRepository.findByStaIdentifier(id, graphBuilder);
    }
}

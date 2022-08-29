package org.n52.sta.data.editor;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.data.entity.FeatureOfInterestData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.repositories.entity.FeatureOfInterestRepository;
import org.n52.sta.data.support.FeatureOfInterestGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

public class FeatureOfInterestEntityEditor extends DatabaseEntityAdapter<AbstractFeatureEntity>
        implements
        EntityEditorDelegate<FeatureOfInterest, FeatureOfInterestData> {

    @Autowired
    private FeatureOfInterestRepository FeatureOfInterestRepository;

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
        throw new EditorException();
    }

    @Override
    public FeatureOfInterestData save(FeatureOfInterest entity) throws EditorException {
        throw new EditorException();
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
        return FeatureOfInterestRepository.findByStaIdentifier(id, graphBuilder);
    }
}

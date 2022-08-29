
package org.n52.sta.data.editor;

import java.util.Optional;

import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.repositories.entity.PhenomenonRepository;
import org.n52.sta.data.support.ObservedPropertyGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class ObservedPropertyEntityEditor extends DatabaseEntityAdapter<PhenomenonEntity> implements
        EntityEditorDelegate<ObservedProperty, ObservedPropertyData> {

    @Autowired
    private PhenomenonRepository phenomenonRepository;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public ObservedPropertyEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.datastreamEditor = (EntityEditorDelegate<Datastream, DatastreamData>)
                getService(Datastream.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public ObservedPropertyData getOrSave(ObservedProperty entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public ObservedPropertyData save(ObservedProperty entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public ObservedPropertyData update(ObservedProperty entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<PhenomenonEntity> getEntity(String id) {
        ObservedPropertyGraphBuilder graphBuilder = ObservedPropertyGraphBuilder.createEmpty();
        return phenomenonRepository.findByStaIdentifier(id, graphBuilder);
    }
}

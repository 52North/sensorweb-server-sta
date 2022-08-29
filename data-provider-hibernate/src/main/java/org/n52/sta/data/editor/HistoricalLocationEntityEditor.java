package org.n52.sta.data.editor;

import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.data.entity.HistoricalLocationData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.HistoricalLocationRepository;
import org.n52.sta.data.support.HistoricalLocationGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

public class HistoricalLocationEntityEditor extends DatabaseEntityAdapter<HistoricalLocationEntity>
        implements
        EntityEditorDelegate<HistoricalLocation, HistoricalLocationData> {

    @Autowired
    private HistoricalLocationRepository historicalLocationRepository;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;

    public HistoricalLocationEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        //@formatter:on
    }

    @Override
    public HistoricalLocationData getOrSave(HistoricalLocation entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public HistoricalLocationData save(HistoricalLocation entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public HistoricalLocationData update(HistoricalLocation entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<HistoricalLocationEntity> getEntity(String id) {
        HistoricalLocationGraphBuilder graphBuilder = HistoricalLocationGraphBuilder.createEmpty();
        return historicalLocationRepository.findByStaIdentifier(id, graphBuilder);
    }
}

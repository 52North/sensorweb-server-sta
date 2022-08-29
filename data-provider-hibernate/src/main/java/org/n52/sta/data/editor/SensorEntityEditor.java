package org.n52.sta.data.editor;

import java.util.Optional;

import org.n52.series.db.beans.ProcedureEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.LocationData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.repositories.entity.ProcedureRepository;
import org.n52.sta.data.support.SensorGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class SensorEntityEditor extends DatabaseEntityAdapter<ProcedureEntity>
        implements
        EntityEditorDelegate<Sensor, SensorData> {

    @Autowired
    private ProcedureRepository procedureRepository;

    private EntityEditorDelegate<Datastream, DatastreamData> datastreamEditor;

    public SensorEntityEditor(EntityServiceLookup serviceLookup) {
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
    public SensorData getOrSave(Sensor entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public SensorData save(Sensor entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public SensorData update(Sensor entity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        throw new EditorException();
    }

    @Override
    protected Optional<ProcedureEntity> getEntity(String id) {
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createEmpty();
        return procedureRepository.findByStaIdentifier(id, graphBuilder);
    }
}

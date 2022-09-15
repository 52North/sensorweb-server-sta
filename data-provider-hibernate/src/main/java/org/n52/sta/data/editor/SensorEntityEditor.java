package org.n52.sta.data.editor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.entity.StaData;
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

    @Autowired
    private ValueHelper valueHelper;

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
        Optional<ProcedureEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new SensorData(e, Optional.empty()))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public SensorData save(Sensor entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        String staIdentifier = entity.getId();
        EntityService<Sensor> service = getService(Sensor.class);

        if (service.exists(staIdentifier)) {
            throw new EditorException("Sensor already exists with Id '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        ProcedureEntity procedureEntity = new ProcedureEntity();
        procedureEntity.setIdentifier(id);
        procedureEntity.setStaIdentifier(id);
        procedureEntity.setName(entity.getName());
        procedureEntity.setDescriptionFile(entity.getMetadata());
        procedureEntity.setDescription(entity.getDescription());

        valueHelper.setFormat(procedureEntity::setFormat, entity.getEncodingType());

        ProcedureEntity saved = procedureRepository.save(procedureEntity);

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(entry -> convertParameter(procedureEntity, entry))
               .forEach(procedureEntity::addParameter);

        // save related entities
        procedureEntity.setDatasets(Streams.stream(entity.getDatastreams())
                                           .map(datastreamEditor::getOrSave)
                                           .map(StaData::getData)
                                           .collect(Collectors.toSet()));

        // we need to flush else updates to relations are not persisted
        procedureRepository.flush();

        return new SensorData(saved, Optional.empty());
    }

    @Override
    public SensorData update(Sensor oldEntity, Sensor updateEntity) throws EditorException {
        throw new EditorException();
    }

    @Override
    public void delete(String id) throws EditorException {
        ProcedureEntity procedure = getEntity(id)
                .orElseThrow(() -> new EditorException("could not find entity with id: " + id));

        procedure.getDatasets().forEach(ds -> {
            datastreamEditor.delete(ds.getStaIdentifier());
        });

        procedureRepository.delete(procedure);
    }

    @Override
    protected Optional<ProcedureEntity> getEntity(String id) {
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createEmpty();
        return procedureRepository.findByStaIdentifier(id, graphBuilder);
    }
}

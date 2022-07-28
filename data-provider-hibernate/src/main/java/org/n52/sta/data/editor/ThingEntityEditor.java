
package org.n52.sta.data.editor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.platform.PlatformBooleanParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformQuantityParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformTextParameterEntity;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.PlatformRepository;
import org.n52.sta.data.support.ThingGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class ThingEntityEditor extends DatabaseEntityAdapter<PlatformEntity> implements EntityEditor<Thing> {

    @Autowired
    private PlatformRepository platformRepository;

    protected ThingEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public ThingData save(Thing entity) throws EditorException {

        String staIdentifier = entity.getId();
        EntityService<Thing> thingService = getService(Thing.class);
        if (thingService.exists(staIdentifier)) {
            throw new EditorException("Thing already exists with ID '" + staIdentifier + "'");
        }

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();
        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setIdentifier(id);
        platformEntity.setStaIdentifier(id);
        platformEntity.setName(entity.getName());
        platformEntity.setDescription(entity.getDescription());

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(this::convertParameter)
               .filter(p -> p != null)
               .forEach(platformEntity::addParameter);

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThingData update(Thing entity) throws EditorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String id) throws EditorException {
        // TODO Auto-generated method stub

    }

    private PlatformParameterEntity< ? > convertParameter(Map.Entry<String, Object> parameter) {
        String key = parameter.getKey();
        Object value = parameter.getValue();

        // TODO review ParameterFactory and DTOTransformerImpl#convertParameters

        Class< ? extends Object> valueType = value.getClass();
        if (Number.class.isAssignableFrom(valueType)) {
            PlatformQuantityParameterEntity parameterEntity = new PlatformQuantityParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue(BigDecimal.valueOf((Double) value));
        } else if (Boolean.class.isAssignableFrom(valueType)) {
            PlatformBooleanParameterEntity parameterEntity = new PlatformBooleanParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((Boolean) value);
        } else if (String.class.isAssignableFrom(valueType)) {
            PlatformTextParameterEntity parameterEntity = new PlatformTextParameterEntity();
            parameterEntity.setName(key);
            parameterEntity.setValue((String) value);
            // } else {
            // // TODO handle other cases from DTOTransformerImpl#convertParameters
        }
        return null;
    }

    @Override
    protected Optional<PlatformEntity> getEntity(String id) {
        ThingGraphBuilder graphBuilder = ThingGraphBuilder.createEmpty();
        return platformRepository.findByStaIdentifier(id, graphBuilder);
    }

}
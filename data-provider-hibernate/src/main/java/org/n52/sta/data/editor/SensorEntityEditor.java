
package org.n52.sta.data.editor;

import java.util.Optional;

import org.n52.series.db.beans.ProcedureEntity;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.repositories.entity.ProcedureRepository;
import org.n52.sta.data.support.SensorGraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class SensorEntityEditor extends DatabaseEntityAdapter<ProcedureEntity> implements EntityEditor<Sensor> {

    @Autowired
    private ProcedureRepository procedureRepository;

    protected SensorEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public SensorData save(Sensor entity) throws EditorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SensorData update(Sensor entity) throws EditorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(String id) throws EditorException {
        // TODO Auto-generated method stub

    }

    @Override
    protected Optional<ProcedureEntity> getEntity(String id) {
        SensorGraphBuilder graphBuilder = SensorGraphBuilder.createEmpty();
        return procedureRepository.findByStaIdentifier(id, graphBuilder);
    }

}

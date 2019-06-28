package org.n52.sta.data.repositories;

import org.n52.series.db.beans.*;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.SpringApplicationContext;
import org.n52.sta.data.STAEventHandler;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MessageBusRepository<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> {

    private JpaEntityInformation entityInformation;

    private STAEventHandler mqttHandler;

    private EntityManager em;


    MessageBusRepository(JpaEntityInformation entityInformation,
                         EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
        this.entityInformation = entityInformation;

        this.mqttHandler = (STAEventHandler) SpringApplicationContext.getBean("mqttEventHandler");
        Assert.notNull(this.mqttHandler, "Could not autowire Mqtt handler!");
    }

    @Transactional
    @Override
    public <S extends T> S save(S newEntity) {
        boolean intercept = mqttHandler.getWatchedEntityTypes().contains(entityInformation.getJavaType().getName());

        if (entityInformation.isNew(newEntity)) {
            em.persist(newEntity);
            em.flush();
            if (intercept) {
                this.mqttHandler.handleEvent(newEntity, null);
            }
        } else {
            if (intercept) {
                S oldEntity = (S) em.find(newEntity.getClass(), entityInformation.getId(newEntity));
                newEntity = em.merge(newEntity);
                em.flush();
                this.mqttHandler.handleEvent(newEntity, computeDifferenceMap(oldEntity, newEntity));
            } else {
                return em.merge(newEntity);
            }
        }

        return newEntity;
    }


    /**
     * Saves an entity to the Datastore without intercepting for mqtt subscription checking.
     * Used when Entity is saved multiple times during creation
     * @param entity Entity to be saved
     * @param <S> raw entity type
     * @return saved entity.
     */
    @Transactional
    public <S extends T> S intermediateSave(S entity) {
        if (entityInformation.isNew(entity)) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    private Set<String> computeDifferenceMap(Object oldE, Object newE) {
        HashSet<String> map = new HashSet<>();
        switch (oldE.getClass().getSimpleName()) {
            case "ProcedureEntity":
                ProcedureEntity oldProcedure = (ProcedureEntity) oldE;
                ProcedureEntity newProcedure = (ProcedureEntity) newE;
                if (!oldProcedure.getDescription().equals(newProcedure.getDescription())) map.add("description");
                if (!oldProcedure.getName().equals(newProcedure.getName())) map.add("name");
                if (!oldProcedure.getDescriptionFile().equals(newProcedure.getDescriptionFile())) map.add("metadata");
                if (!oldProcedure.getFormat().getFormat().equals(newProcedure.getFormat().getFormat()))
                    map.add("encodingType");
                return map;
            case "LocationEntity":
                LocationEntity oldLocation = (LocationEntity) oldE;
                LocationEntity newLocation = (LocationEntity) newE;
                if (!oldLocation.getDescription().equals(newLocation.getDescription())) map.add("description");
                if (!oldLocation.getName().equals(newLocation.getName())) map.add("name");
                if (!oldLocation.getGeometryEntity().getGeometry().equals(newLocation.getGeometryEntity().getGeometry())) map.add("location");
                if (!oldLocation.getLocation().equals(newLocation.getLocation())) map.add("location");
                if (!oldLocation.getLocationEncoding().getEncodingType().equals(newLocation.getLocationEncoding().getEncodingType()))
                    map.add("encodingType");
                return map;
            case "PlatformEntity":
                PlatformEntity oldThing = (PlatformEntity) oldE;
                PlatformEntity newThing = (PlatformEntity) newE;
                if (!oldThing.getDescription().equals(newThing.getDescription())) map.add("description");
                if (!oldThing.getName().equals(newThing.getName())) map.add("name");
                if (!oldThing.getProperties().equals(newThing.getProperties())) map.add("properties");
                return map;
            case "DatastreamEntity":
                DatastreamEntity oldDatastream = (DatastreamEntity) oldE;
                DatastreamEntity newDatastream = (DatastreamEntity) newE;
                if (!oldDatastream.getDescription().equals(newDatastream.getDescription())) map.add("description");
                if (!oldDatastream.getName().equals(newDatastream.getName())) map.add("name");
                if (!oldDatastream.getObservationType().getFormat().equals(newDatastream.getObservationType().getFormat()))
                    map.add("observationType");
                if (!oldDatastream.getUnitOfMeasurement().equals(newDatastream.getUnitOfMeasurement()))
                    map.add("unitOfMeasurement");
                if (!oldDatastream.getGeometryEntity().getGeometry().equals(newDatastream.getGeometryEntity().getGeometry()))
                    map.add("observedArea");
                if (!oldDatastream.getSamplingTimeStart().equals(newDatastream.getSamplingTimeStart()))
                    map.add("phenomenonTime");
                if (!oldDatastream.getSamplingTimeEnd().equals(newDatastream.getSamplingTimeEnd()))
                    map.add("phenomenonTime");
                if (!oldDatastream.getResultTimeStart().equals(newDatastream.getResultTimeStart()))
                    map.add("resultTime");
                if (!oldDatastream.getResultTimeEnd().equals(newDatastream.getResultTimeEnd())) map.add("resultTime");
                return map;
            case "HistoricalLocationEntity":
                HistoricalLocationEntity oldHLocation = (HistoricalLocationEntity) oldE;
                HistoricalLocationEntity newHLocation = (HistoricalLocationEntity) newE;
                if (!oldHLocation.getTime().equals(newHLocation.getTime())) map.add("time");
                return map;
            case "DataEntity":
                DataEntity<?> oldData = (DataEntity<?>) oldE;
                DataEntity<?> newData = (DataEntity<?>) newE;
                if (!oldData.getSamplingTimeStart().equals(newData.getSamplingTimeStart())) map.add("phenomenonTime");
                if (!oldData.getSamplingTimeEnd().equals(newData.getSamplingTimeEnd())) map.add("phenomenonTime");
                if (!oldData.getResultTime().equals(newData.getResultTime())) map.add("resultTime");
                if (!oldData.getValidTimeStart().equals(newData.getValidTimeStart())) map.add("validTime");
                if (!oldData.getValidTimeEnd().equals(newData.getValidTimeEnd())) map.add("validTime");
                //TODO: implement difference map for ::getParameters and ::getResult and "resultQuality"
                return map;
            case "FeatureEntity":
                FeatureEntity oldFeature = (FeatureEntity) oldE;
                FeatureEntity newFeature = (FeatureEntity) newE;
                if (!oldFeature.getName().equals(newFeature.getName())) map.add("name");
                if (!oldFeature.getDescription().equals(newFeature.getDescription())) map.add("description");
                if (!oldFeature.getGeometry().equals(newFeature.getGeometry())) map.add("feature");
                // There is only a single allowed encoding type so it cannot change
                return map;
            case "PhenomenonEntity":
                PhenomenonEntity oldPhenom = (PhenomenonEntity) oldE;
                PhenomenonEntity newPhenom = (PhenomenonEntity) newE;
                if (!oldPhenom.getName().equals(newPhenom.getName())) map.add("name");
                if (!oldPhenom.getDescription().equals(newPhenom.getDescription())) map.add("description");
                if (!oldPhenom.getIdentifier().equals(newPhenom.getIdentifier())) map.add("definition");
                return map;
            default:
                return map;
        }
    }

}

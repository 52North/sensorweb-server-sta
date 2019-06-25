package org.n52.sta.data.repositories;

import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.SpringApplicationContext;
import org.n52.sta.data.STAEventHandler;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
    }

    @PostConstruct
    private void test() {
        System.out.println("test");
    }

    @Transactional
    @Override
    public <S extends T> S save(S newEntity) {
        // Todo check if getname is the correct method
        boolean intercept = mqttHandler.getWatchedEntityTypes().contains(entityInformation.getJavaType().getSimpleName());

        if (entityInformation.isNew(newEntity)) {
            em.persist(newEntity);
            if (intercept) {
                this.mqttHandler.handleEvent(newEntity, null);
            }
        } else {
            if (intercept) {
                S oldEntity = (S) em.find(newEntity.getClass(), entityInformation.getId(newEntity));
                newEntity = em.merge(newEntity);
                this.mqttHandler.handleEvent(newEntity, computeDifferenceMap(oldEntity, newEntity));
            } else {
                return em.merge(newEntity);
            }
        }

        return newEntity;
    }

    private Set<String> computeDifferenceMap(Object oldE, Object newE) {
        HashSet<String> map = new HashSet<>();
        switch (oldE.getClass().getSimpleName()) {
            case "ProcedureEntity":
                ProcedureEntity oldProcedure = (ProcedureEntity) oldE;
                ProcedureEntity newProcedure = (ProcedureEntity) newE;
                if (oldProcedure.getDescription().equals(newProcedure.getDescription())) map.add("description");
                if (oldProcedure.getName().equals(newProcedure.getName())) map.add("name");
                if (oldProcedure.getDescriptionFile().equals(newProcedure.getDescriptionFile())) map.add("metadata");
                if (oldProcedure.getFormat().getFormat().equals(newProcedure.getFormat().getFormat())) map.add("encodingType");
                return map;
            case "LocationEntity":
                LocationEntity oldLocation = (LocationEntity) oldE;
                LocationEntity newLocation = (LocationEntity) newE;
                if (oldLocation.getDescription().equals(newLocation.getDescription())) map.add("description");
                if (oldLocation.getName().equals(newLocation.getName())) map.add("name");
                if (oldLocation.getGeometryEntity().equals(newLocation.getGeometryEntity())) map.add("location");
                if (oldLocation.getLocation().equals(newLocation.getLocation())) map.add("location");
                if (oldLocation.getLocationEncoding().getEncodingType().equals(newLocation.getLocationEncoding().getEncodingType())) map.add("encodingType");
                return map;
            case "PlatformEntity":
                PlatformEntity oldThing = (PlatformEntity) oldE;
                PlatformEntity newThing = (PlatformEntity) newE;
                if (oldThing.getDescription().equals(newThing.getDescription())) map.add("description");
                if (oldThing.getName().equals(newThing.getName())) map.add("name");
                if (oldThing.getProperties().equals(newThing.getProperties())) map.add("properties");
                return map;
                //TODO: implement missing stuff
            case "DatastreamEntity":
            case "HistoricalLocationEntity":
            case "DataEntity":
                //TODO: check if this abstract type is enough or concrete type is needed
            case "FeatureEntity":
            case "PhenomenonEntity":
            default:
                return map;
        }
    }

}

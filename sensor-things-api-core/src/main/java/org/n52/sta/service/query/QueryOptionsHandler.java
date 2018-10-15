/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.utils.EntityAnnotator;
import org.n52.sta.utils.EntityCreationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to handle query options
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class QueryOptionsHandler {

    @Autowired
    private EntityServiceRepository serviceRepository;
    
    @Autowired
    EntityCreationHelper entityCreationHelper;
    
    @Autowired
    EntityAnnotator entityAnnotator;

    protected UriHelper uriHelper;
    
    public UriHelper getUriHelper() {
        return uriHelper;
    }

    public void setUriHelper(UriHelper uriHelper) {
        this.uriHelper = uriHelper;
    }

    /**
     *
     * @param option the {@Link SelectOption} to get the select list from
     * @param edmEntityType the {@Link EdmEntityType} the select list option is
     * referred to
     * @return the select list
     * @throws SerializerException
     */
    public String getSelectListFromSelectOption(EdmEntityType edmEntityType, ExpandOption expandOption, SelectOption selectOption) throws SerializerException {
        String selectList = uriHelper.buildContextURLSelectList(edmEntityType,
                expandOption, selectOption);

        return selectList;
    }

    /**
     * Handles the $expand Query Parameter
     * 
     * @param expandOption Options for expand Parameter
     * @param sourceId Id of the source Entity
     * @param sourceEdmEntitySet EntitySet of the source Entity
     * @param baseURI baseURI of the Request
     * @return List<Link> List of inlined Entities
     */
    public List<Link> handleExpandOption(ExpandOption expandOption, Long sourceId, EdmEntitySet sourceEdmEntitySet, String baseURI) {
        List<Link> links = new ArrayList<>();
        
        expandOption.getExpandItems().forEach(expandItem -> {
            EdmNavigationProperty edmNavigationProperty = null;
            EdmEntityType targetEdmEntityType = null;
            String targetTitle = null;
                
            // TODO: Expand to support nested paths
            UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    
            if(uriResource instanceof UriResourceNavigation) {
                edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
            }
    
            // Get Target Type and Name
            if(edmNavigationProperty != null) {
                targetEdmEntityType = edmNavigationProperty.getType();
                targetTitle = edmNavigationProperty.getName();
            }
            

            Link entity = new Link();
            entity.setTitle(targetTitle);
            
            // Either add inline Collection or add single inline Entity          
            if (sourceEdmEntitySet.getEntityType().getNavigationProperty(targetTitle).isCollection()) {
                entity.setInlineEntitySet(getInlineEntityCollection(sourceId,
                                                                    sourceEdmEntitySet,
                                                                    targetEdmEntityType,
                                                                    new ExpandItemQueryOptions(expandItem, baseURI)));
                
                // Annotate inline Entites with appropiate links
                final EdmEntityType type = targetEdmEntityType;
                entity.getInlineEntitySet().forEach( inlineEntity -> {entityAnnotator.annotateEntity(inlineEntity, type, baseURI);});
            } else {
                entity.setInlineEntity(getInlineEntity(sourceId,
                                                       sourceEdmEntitySet,
                                                       targetEdmEntityType));
                
                // Annotate inline Entites with appropiate links
                entityAnnotator.annotateEntity(entity.getInlineEntity(), targetEdmEntityType, baseURI);    
            }
            // Only add valid Elements
            if (entity != null) {
                links.add(entity);
            }
        });
        
        return links;
    }

    private Entity getInlineEntity(Long sourceId, EdmEntitySet sourceEntitySet, EdmEntityType targetType) {
        AbstractSensorThingsEntityService<?> responseService = serviceRepository.getEntityService(targetType.getName());
        Entity entity = responseService.getRelatedEntity(sourceId, sourceEntitySet.getEntityType());

        return entity;
    }
    
    private EntityCollection getInlineEntityCollection(Long sourceId, EdmEntitySet sourceEntitySet, EdmEntityType targetType, QueryOptions queryOptions) {
        AbstractSensorThingsEntityService<?> responseService = serviceRepository.getEntityService(targetType.getName());
        EntityCollection entityCollection = responseService.getRelatedEntityCollection(sourceId, sourceEntitySet.getEntityType(), queryOptions);

        return entityCollection;
    }

}

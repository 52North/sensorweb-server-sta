/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
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
    public String getSelectListFromSelectOption(SelectOption option, EdmEntityType edmEntityType) throws SerializerException {
        String selectList = uriHelper.buildContextURLSelectList(edmEntityType,
                null, option);

        return selectList;
    }

    
    public List<Link> handleExpandOption(ExpandOption expandOption, Long sourceId, EdmEntitySet sourceEdmEntitySet, String baseURI) {
        List<Link> links = new ArrayList<>();
        
        expandOption.getExpandItems().forEach(expandItem -> {
            EdmNavigationProperty edmNavigationProperty = null;
            EdmEntityType targetEdmEntityType = null;
            String targetTitle = null;
                
            // TODO: Expand to nested paths
            UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
    
            // we don't need to handle error cases, as it is done in the Olingo library
            if(uriResource instanceof UriResourceNavigation) {
                edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
            }
    
            if(edmNavigationProperty != null) {
                targetEdmEntityType = edmNavigationProperty.getType();
                targetTitle = edmNavigationProperty.getName();
            }
            Link entity = resolveExpandItem(sourceId,
                                            sourceEdmEntitySet,
                                            targetEdmEntityType,
                                            targetTitle);
            entityAnnotator.annotateEntity(entity.getInlineEntity(), targetEdmEntityType, baseURI);
            
            links.add(entity);
        });
        
        return links;
    }

    private Link resolveExpandItem(Long sourceId, EdmEntitySet sourceEntitySet, EdmEntityType targetType, String targetTitle) {
        AbstractSensorThingsEntityService<?> responseService = serviceRepository.getEntityService(targetType.getName());
        Entity entity = responseService.getRelatedEntity(sourceId, sourceEntitySet.getEntityType());

        Link link = new Link();
        link.setTitle(targetTitle);
        link.setInlineEntity(entity);
        return link;
    }
    
    private Link resolveExpandItemCollection(Long sourceId, EdmEntitySet sourceEntitySet, EdmEntityType targetType, String targetTitle) {
        AbstractSensorThingsEntityService<?> responseService = serviceRepository.getEntityService(targetType.getName());
        Entity entity = responseService.getRelatedEntity(sourceId, sourceEntitySet.getEntityType());

        Link link = new Link();
        link.setTitle(targetTitle);
        link.setInlineEntity(entity);
        return link;
        
    }

}

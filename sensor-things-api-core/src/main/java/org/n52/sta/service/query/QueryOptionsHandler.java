/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.n52.sta.service.query;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.core.uri.queryoption.ExpandItemImpl;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
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
     * @param edmEntityType
     *        the {@Link EdmEntityType} the select list option is
     * @param expandOption
     *        the {@Link ExpandOption} to get the expand items from
     * @param selectOption
     *        the {@Link SelectOption} to get the select list from referred to
     * @return the select list
     * @throws SerializerException
     */
    public String getSelectListFromSelectOption(EdmEntityType edmEntityType,
                                                ExpandOption expandOption,
                                                SelectOption selectOption)
            throws SerializerException {
        String selectList = uriHelper.buildContextURLSelectList(edmEntityType,
                                                                expandOption,
                                                                selectOption);

        return selectList;
    }

    /**
     * Handles the $expand Query Parameter
     *
     * @param entity
     *        The entity to handle expand parameter for
     * @param expandOption
     *        Options for expand Parameter
     * @param sourceId
     *        Id of the source Entity
     * @param sourceEdmEntityType
     *        EntityType of the source Entity
     * @param baseURI
     *        baseURI of the Request
     * @return List<Link> List of inlined Entities
     */
    public void handleExpandOption(Entity entity,
                                    ExpandOption expandOption,
                                    Long sourceId,
                                    EdmEntityType sourceEdmEntityType,
                                    String baseURI) {
        List<ExpandItem> minimized = minimizeExpandList(expandOption.getExpandItems());
        minimized.forEach(expandItem -> {
            EdmNavigationProperty edmNavigationProperty = null;
            EdmEntityType targetEdmEntityType = null;
            String targetTitle = null;

            UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);

            if (uriResource instanceof UriResourceNavigation) {
                edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
            }

            // Get Target Type and Name
            if (edmNavigationProperty != null) {
                targetEdmEntityType = edmNavigationProperty.getType();
                targetTitle = edmNavigationProperty.getName();
            }

            Link link = entity.getNavigationLink(targetTitle);

            // Either add inline Collection or add single inline Entity
            if (sourceEdmEntityType.getNavigationProperty(targetTitle).isCollection()) {
                try {
                    link.setInlineEntitySet(getInlineEntityCollection(sourceId,
                                                                      sourceEdmEntityType,
                                                                      targetEdmEntityType,
                                                                      new ExpandItemQueryOptions(expandItem, baseURI)));
                } catch (ODataApplicationException e) {}

            } else {
                link.setInlineEntity(getInlineEntity(sourceId,
                                                     sourceEdmEntityType,
                                                     targetEdmEntityType,
                                                     new ExpandItemQueryOptions(expandItem, baseURI)));
            }
        });
    }

    private Entity getInlineEntity(Long sourceId,
                                   EdmEntityType sourceType,
                                   EdmEntityType targetType,
                                   QueryOptions queryOptions) {
        AbstractSensorThingsEntityService< ? , ? > responseService = serviceRepository.getEntityService(targetType.getName());
        Entity entity = responseService.getRelatedEntity(sourceId, sourceType);

        if (queryOptions.hasExpandOption()) {
            entityAnnotator.annotateEntity(entity,
                                           targetType,
                                           queryOptions.getBaseURI(),
                                           queryOptions.getSelectOption());
            String id = entity.getProperty(PROP_ID).getValue().toString();
            handleExpandOption(entity,
                               queryOptions.getExpandOption(),
                               Long.parseLong(id),
                               targetType,
                               queryOptions.getBaseURI());
        } else {
            entityAnnotator.annotateEntity(entity,
                                           targetType,
                                           queryOptions.getBaseURI(),
                                           queryOptions.getSelectOption());
        }

        return entity;
    }

    private EntityCollection getInlineEntityCollection(Long sourceId,
                                                       EdmEntityType sourceType,
                                                       EdmEntityType targetType,
                                                       QueryOptions queryOptions)
            throws ODataApplicationException {
        AbstractSensorThingsEntityService< ? , ? > responseService = serviceRepository.getEntityService(targetType.getName());
        EntityCollection entityCollection = responseService.getRelatedEntityCollection(sourceId,
                                                                                       sourceType,
                                                                                       queryOptions);

        if (queryOptions.hasCountOption()) {
            long count = responseService.getRelatedEntityCollectionCount(sourceId, sourceType);
            entityCollection.setCount(Long.valueOf(count).intValue());
        }

        if (queryOptions.hasExpandOption()) {
            entityCollection.forEach(entity -> {
                entityAnnotator.annotateEntity(entity,
                                               targetType,
                                               queryOptions.getBaseURI(),
                                               queryOptions.getSelectOption());
                String id = entity.getProperty(PROP_ID).getValue().toString();
                handleExpandOption(entity,
                                   queryOptions.getExpandOption(),
                                   Long.parseLong(id),
                                   targetType,
                                   queryOptions.getBaseURI());
            });
        } else {
            entityCollection.forEach(entity -> {
                entityAnnotator.annotateEntity(entity,
                                               targetType,
                                               queryOptions.getBaseURI(),
                                               queryOptions.getSelectOption());
            });
        }

        return entityCollection;
    }
    
    public static ExpandOption minimizeExpandOption(ExpandOption option) {
        if (option == null) {
            return null;
        } else {
            ExpandOptionImpl newExpandOption = new ExpandOptionImpl();
            minimizeExpandList(option.getExpandItems()).forEach(newExpandOption::addExpandItem);
            return newExpandOption;
        }
    }

    /**
     * Minimizes a given List of ExpandItems by combining Items that relate to the same Entity.
     * 
     * @param original
     *        List of ExpandItems to be reduced.
     * @return Minimal set of ExpandItems
     */
    // TODO: Improve key (e.g. so the following request has different Keys for each Datastream):
    // [...]/Things?$expand=Datastreams($filter=id eq 2)/Sensor,Datastreams($filter=id ne 2)/Observations
    private static List<ExpandItem> minimizeExpandList(List<ExpandItem> original) {
        if (original.size() <= 1) {
            return original;
        } else {
            Map<String, ExpandItem> resultMap = new HashMap<String, ExpandItem>();
            original.forEach(item -> {
                String key = item.getResourcePath().getUriResourceParts().get(0).toString();
                if (resultMap.containsKey(key)) {
                    // Join Items
                    resultMap.put(key, joinExpandItems(item, resultMap.get(key)));
                } else {
                    resultMap.put(key, item);
                }
            });
            return new ArrayList<ExpandItem>(resultMap.values());
        }
    }

    /**
     * Joins two ExpandItems relating to the same Entity by joining all subsequent Items
     * 
     * @param first
     *        First Item
     * @param second
     *        Second Item
     * @return ExpandItem
     *         joined Item
     */
    private static ExpandItem joinExpandItems(ExpandItem first, ExpandItem second) {
        ExpandItemImpl result = new ExpandItemImpl();
        result.setSystemQueryOptions(Arrays.asList(first.getCountOption(), second.getCountOption()));
        
        ExpandOptionImpl newExpandOption = new ExpandOptionImpl();
        first.getExpandOption().getExpandItems().forEach(newExpandOption::addExpandItem);
        second.getExpandOption().getExpandItems().forEach(newExpandOption::addExpandItem);
        result.setSystemQueryOption(newExpandOption);
        
        result.setSystemQueryOptions(Arrays.asList(first.getFilterOption(), second.getFilterOption()));
        result.setSystemQueryOptions(Arrays.asList(first.getOrderByOption(), second.getOrderByOption()));
        result.setSystemQueryOptions(Arrays.asList(first.getSearchOption(), second.getSearchOption()));
        result.setSystemQueryOptions(Arrays.asList(first.getSelectOption(), second.getSelectOption()));
        result.setSystemQueryOptions(Arrays.asList(first.getSkipOption(), second.getSkipOption()));
        result.setSystemQueryOptions(Arrays.asList(first.getTopOption(), second.getTopOption()));
        result.setResourcePath(first.getResourcePath());
        return result;
    }

}

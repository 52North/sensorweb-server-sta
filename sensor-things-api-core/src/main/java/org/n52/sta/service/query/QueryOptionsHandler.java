/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.query;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
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
     * @param edmEntityType the {@Link EdmEntityType} the select list option is
     * @param expandOption the {@Link ExpandOption} to get the expand items from
     * @param selectOption the {@Link SelectOption} to get the select list from
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
     * @param entity The entity to handle expand parameter for
     * @param expandOption Options for expand Parameter
     * @param sourceId Id of the source Entity
     * @param sourceEdmEntityType EntityType of the source Entity
     * @param baseURI baseURI of the Request
     * @return List<Link> List of inlined Entities
     */
    public List<Link> handleExpandOption(Entity entity, ExpandOption expandOption, Long sourceId, EdmEntityType sourceEdmEntityType, String baseURI) {
        List<Link> links = new ArrayList<>();

        expandOption.getExpandItems().forEach(expandItem -> {
            EdmNavigationProperty edmNavigationProperty = null;
            EdmEntityType targetEdmEntityType = null;
            String targetTitle = null;

            // TODO: Expand to support nested paths
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

                // Annotate inline Entites with appropiate links
                final EdmEntityType type = targetEdmEntityType;
            } else {
                link.setInlineEntity(getInlineEntity(sourceId,
                        sourceEdmEntityType,
                        targetEdmEntityType,
                        new ExpandItemQueryOptions(expandItem, baseURI)));

                // Annotate inline Entites with appropiate links
                entityAnnotator.annotateEntity(link.getInlineEntity(), targetEdmEntityType, baseURI, (new ExpandItemQueryOptions(expandItem, baseURI)).getSelectOption());
            }
            // Only add valid Elements
            if (link != null) {
                links.add(link);
            }
        });

        return links;
    }

    private Entity getInlineEntity(Long sourceId, EdmEntityType sourceType, EdmEntityType targetType, QueryOptions queryOptions) {
        AbstractSensorThingsEntityService<?,?> responseService = serviceRepository.getEntityService(targetType.getName());
        Entity entity = responseService.getRelatedEntity(sourceId, sourceType);

        if (queryOptions.hasExpandOption()) {
            entityAnnotator.annotateEntity(entity, targetType, queryOptions.getBaseURI(), queryOptions.getSelectOption());
            List<Link> links = handleExpandOption(entity, queryOptions.getExpandOption(),
                    Long.parseLong(entity.getProperty(PROP_ID).getValue().toString()),
                    targetType,
                    queryOptions.getBaseURI());
        } else {
            entityAnnotator.annotateEntity(entity, targetType, queryOptions.getBaseURI(), queryOptions.getSelectOption());
        }

        return entity;
    }

    private EntityCollection getInlineEntityCollection(Long sourceId, EdmEntityType sourceType, EdmEntityType targetType, QueryOptions queryOptions) throws ODataApplicationException {
        AbstractSensorThingsEntityService<?,?> responseService = serviceRepository.getEntityService(targetType.getName());
        EntityCollection entityCollection = responseService.getRelatedEntityCollection(sourceId, sourceType, queryOptions);

        long count = responseService.getRelatedEntityCollectionCount(sourceId, sourceType);

        if (queryOptions.hasCountOption()) {
            entityCollection.setCount(Long.valueOf(count).intValue());
        }

        if (queryOptions.hasExpandOption()) {
            entityCollection.forEach(entity -> {
                entityAnnotator.annotateEntity(entity, targetType, queryOptions.getBaseURI(), queryOptions.getSelectOption());
                List<Link> links = handleExpandOption(entity, queryOptions.getExpandOption(),
                        Long.parseLong(entity.getProperty(PROP_ID).getValue().toString()),
                        targetType,
                        queryOptions.getBaseURI());
            });
        } else {
            entityCollection.forEach(entity -> {
                entityAnnotator.annotateEntity(entity, targetType, queryOptions.getBaseURI(), queryOptions.getSelectOption());
            });
        }

        return entityCollection;
    }

}

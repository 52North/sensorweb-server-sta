/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.query;

import java.util.ArrayList;
import java.util.List;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.n52.sta.data.service.EntityServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to handle query options
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class QueryOptionsHandler {

    protected UriHelper uriHelper;
    @Autowired
    EntityServiceRepository entityServiceRepository;

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

    public List<Link> handleExpandOption(ExpandOption option, EdmEntitySet entitySet) {
        List<Link> links = new ArrayList();
        option.getExpandItems().forEach(i -> {
            links.add(resolveExpandItem(i, entitySet));
        });
        return links;
    }

    private Link resolveExpandItem(ExpandItem item, EdmEntitySet entitySet) {
        Link link = new Link();
        // TODO: Link creation from ExpandItem
        return link;
    }

}

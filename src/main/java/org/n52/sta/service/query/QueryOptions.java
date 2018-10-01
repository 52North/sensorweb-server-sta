package org.n52.sta.service.query;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;

public class QueryOptions {

    private static final int DEFAULT_TOP = 100;
    private final UriInfo uriInfo;
    private final String baseURI;

    public QueryOptions(UriInfo uriInfo, String baseURI) {
        this.uriInfo = uriInfo;
        this.baseURI = baseURI;
    }
    
    /**
     * @return the uriInfo
     */
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    /**
     * @return the baseURI
     */
    public String getBaseURI() {
        return baseURI;
    }

    public boolean hasCountOption() {
        return getUriInfo().getCountOption() != null && getUriInfo().getCountOption().getValue();
    }
    
    public CountOption getCountOption() {
        return getUriInfo().getCountOption();
    }
    
    private boolean hasTopOption() {
        return getUriInfo().getTopOption() != null;
    }
    
    public TopOption getTopOption() {
        if (hasTopOption()) {
            return getUriInfo().getTopOption();
        }
        return new TopOptionImpl().setValue(DEFAULT_TOP);
    }
    
    public boolean hasSkipOption() {
        return getUriInfo().getSkipOption() != null;
    }
    
    public SkipOption getSkipOption() {
        return getUriInfo().getSkipOption();
    }

}

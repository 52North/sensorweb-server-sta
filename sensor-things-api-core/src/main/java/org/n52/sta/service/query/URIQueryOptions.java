package org.n52.sta.service.query;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;

/**
 * Class that holds the {@link UriInfo} to get the {@link SystemQueryOption}s to
 * use them in the {@link AbstractSensorThingsEntityService}s.
 *
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 1.0.0
 *
 */
public class URIQueryOptions implements QueryOptions {

    private final UriInfo uriInfo;

    private final String baseURI;

    /**
     * Constructor
     *
     * @param uriInfo the {@link UriInfo} of the query
     * @param baseURI the baseURI
     */
    public URIQueryOptions(UriInfo uriInfo, String baseURI) {
        this.uriInfo = uriInfo;
        this.baseURI = baseURI;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getUriInfo()
     */
    @Override
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getBaseURI()
     */
    @Override
    public String getBaseURI() {
        return baseURI;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasCountOption()
     */
    @Override
    public boolean hasCountOption() {
        return getUriInfo().getCountOption() != null && getUriInfo().getCountOption().getValue();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getCountOption()
     */
    @Override
    public CountOption getCountOption() {
        return getUriInfo().getCountOption();
    }
    
    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getTopOption()
     */
    private boolean hasTopOption() {
        return getUriInfo().getTopOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getTopOption()
     */
    @Override
    public TopOption getTopOption() {
        if (hasTopOption() && getUriInfo().getTopOption().getValue() <= DEFAULT_TOP) {
            return getUriInfo().getTopOption();
        }
        return new TopOptionImpl().setValue(DEFAULT_TOP);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasSkipOption()
     */
    @Override
    public boolean hasSkipOption() {
        return getUriInfo().getSkipOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getSkipOption()
     */
    @Override
    public SkipOption getSkipOption() {
        return getUriInfo().getSkipOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasOrderByOption()
     */
    @Override
    public boolean hasOrderByOption() {
        return getUriInfo().getOrderByOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getOrderByOption()
     */
    @Override
    public OrderByOption getOrderByOption() {
        return getUriInfo().getOrderByOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasSelectOption()
     */
    @Override
    public boolean hasSelectOption() {
        return getUriInfo().getSelectOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getSelectOption()
     */
    @Override
    public SelectOption getSelectOption() {
        return getUriInfo().getSelectOption();
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#hasExpandOption()
     */
    @Override
    public boolean hasExpandOption() {
        return getUriInfo().getExpandOption() != null;
    }

    /* (non-Javadoc)
     * @see org.n52.sta.service.query.QueryOptions#getExpandOption()
     */
    @Override
    public ExpandOption getExpandOption() {
        return getUriInfo().getExpandOption();
    }
}

package org.n52.sta.service.query;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
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
public class QueryOptions {

    private static final int DEFAULT_TOP = 100;

    private final UriInfo uriInfo;

    private final String baseURI;

    /**
     * Constuctor
     * 
     * @param uriInfo
     *            the {@link UriInfo} of the query
     * @param baseURI
     *            the baseURI
     */
    public QueryOptions(UriInfo uriInfo, String baseURI) {
        this.uriInfo = uriInfo;
        this.baseURI = baseURI;
    }

    /**
     * Get the {@link UriInfo}
     * 
     * @return the uriInfo
     */
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    /**
     * Get the baseURI
     * 
     * @return the baseURI
     */
    public String getBaseURI() {
        return baseURI;
    }

    /**
     * Check if the {@link UriInfo} holds {@link CountOption}
     * 
     * @return <code>true</code>, if the {@link UriInfo} holds
     *         {@link CountOption}
     */
    public boolean hasCountOption() {
        return getUriInfo().getCountOption() != null && getUriInfo().getCountOption().getValue();
    }

    /**
     * Get the {@link CountOption} from {@link UriInfo}
     * 
     * @return the {@link CountOption}
     */
    public CountOption getCountOption() {
        return getUriInfo().getCountOption();
    }

    private boolean hasTopOption() {
        return getUriInfo().getTopOption() != null;
    }

    /**
     * Get the {@link TopOption} from {@link UriInfo} or the default with 100
     * 
     * @return the {@link TopOption}
     */
    public TopOption getTopOption() {
        if (hasTopOption() && getUriInfo().getTopOption().getValue() <= DEFAULT_TOP) {
            return getUriInfo().getTopOption();
        }
        return new TopOptionImpl().setValue(DEFAULT_TOP);
    }

    /**
     * Check if the {@link UriInfo} holds {@link SkipOption}
     * 
     * @return <code>true</code>, if the {@link UriInfo} holds
     *         {@link SkipOption}
     */
    public boolean hasSkipOption() {
        return getUriInfo().getSkipOption() != null;
    }

    /**
     * Get the {@link SkipOption} from {@link UriInfo}
     * 
     * @return the {@link SkipOption}
     */
    public SkipOption getSkipOption() {
        return getUriInfo().getSkipOption();
    }

    /**
     * Check if the {@link UriInfo} holds {@link OrderByOption}
     * 
     * @return <code>true</code>, if the {@link UriInfo} holds
     *         {@link OrderByOption}
     */
    public boolean hasOrderByOption() {
        return getUriInfo().getOrderByOption() != null;
    }

    /**
     * Get the {@link OrderByOption} from {@link UriInfo}
     * 
     * @return the {@link OrderByOption}
     */
    public OrderByOption getOrderByOption() {
        return getUriInfo().getOrderByOption();
    }

}

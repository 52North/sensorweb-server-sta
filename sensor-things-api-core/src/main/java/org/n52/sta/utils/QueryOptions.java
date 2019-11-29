package org.n52.sta.utils;

import org.n52.sta.data.service.AbstractSensorThingsEntityService;

import java.util.Set;

/**
 * Abstract Interface to hold Query Parameters for {@link AbstractSensorThingsEntityService}
 *
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public interface QueryOptions {

    int DEFAULT_TOP = 100;

    /**
     * Get the baseURI
     *
     * @return the baseURI
     */
    // String getBaseURI();

    /**
     * @return <code>true</code>, if $count option is present
     */
    boolean hasCountOption();

    /**
     * @return return true if count option is present
     */
    boolean getCountOption();

    /**
     * Get the value of the top query option. If missing returns {@link QueryOptions#DEFAULT_TOP}
     *
     * @return value of $top query option
     */
    int getTopOption();

    /**
     * @return <code>true</code>, if $skip option is present
     */
    boolean hasSkipOption();

    /**
     * @return the value of $skip option
     */
    int getSkipOption();

    /**
     * @return <code>true</code>, if $orderby option is present
     */
    boolean hasOrderByOption();

    /**
     * @return the value of $orderby option
     */
    String getOrderByOption();

    /**
     * @return <code>true</code>, if the $select is present
     */
    boolean hasSelectOption();

    /**
     * @return the value of $select option
     */
    Set<String> getSelectOption();

    /**
     * @return <code>true</code>, if $expand option is present
     */
    boolean hasExpandOption();

    /**
     * @return the value of $expand option
     */
    Set<String> getExpandOption();

    /**
     * @return <code>true</code>, if $filter option is present
     */
    boolean hasFilterOption();

    /**
     * @return the value of $filter option
     */
    Set<String> getFilterOption();

}
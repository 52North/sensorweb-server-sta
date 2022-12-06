package org.n52.sta.mqtt.core.subscription;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.utils.RequestUtils;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class MqttSelectSubscription extends MqttEntityCollectionSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSelectSubscription.class);

    private String selectOption;

    private QueryOptions queryOptions;

    public MqttSelectSubscription(String topic, Matcher mt) {
        super(topic, mt, true);

        selectOption = mt.group(RequestUtils.GROUPNAME_SELECT);
        Assert.notNull(selectOption, "Unable to parse topic. Could not extract selectOption");

        QueryOptionsFactory qof = new QueryOptionsFactory();
        HashSet<FilterClause> filters = new HashSet<>();
        HashSet<String> filterItems = new HashSet<>();
        Collections.addAll(filterItems, mt.group(RequestUtils.GROUPNAME_SELECT).split(","));
        filters.add(new SelectFilter(filterItems));
        queryOptions = qof.createQueryOptions(filters);
        LOGGER.debug(this.toString());
    }

    @Override
    public String toString() {
        String base = super.toString();
        return new StringBuilder()
            .append(base)
            .deleteCharAt(base.length() - 1)
            .append(",selectOption=")
            .append(selectOption)
            .append("]")
            .toString();
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }
}

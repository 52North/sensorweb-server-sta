package org.n52.sta.mqtt.core.subscription;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.service.STARequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.regex.Matcher;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class MqttSelectSubscription extends MqttEntityCollectionSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSelectSubscription.class);

    private String selectOption;

    private QueryOptions queryOptions;

    public MqttSelectSubscription(String topic, Matcher mt) {
        super(topic, mt);

        selectOption = mt.group(STARequestUtils.GROUPNAME_SELECT);
        Assert.notNull(selectOption, "Unable to parse topic. Could not extract selectOption");

        queryOptions = new QueryOptions(getTopic());
        LOGGER.debug(this.toString());
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
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
}

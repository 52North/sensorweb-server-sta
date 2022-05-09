package org.n52.sta.data.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.entity.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = DataProviderTestConfiguration.class)
public class ThingEntityProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingEntityProviderTest.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ThingEntityProvider provider;

    @Test
    public void test1() {
        assertThat(context, is(not(Matchers.nullValue())));
    }

    @Test
    public void test() {
        LOGGER.debug("Testing starts");
        EntityPage<Thing> emptyPage = provider.getEntities();
        assertThat(emptyPage.getTotalCount(), is(0));
    }

}

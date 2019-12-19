//@SpringBootConfiguration
//@EnableAutoConfiguration
//@AutoConfigureTestDatabase
//@DataJpaTest

package org.n52.sta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * This class tests various different things not covered directly by the official cite tests
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Profile("test")
public class MainTest {

    protected final static String jsonMimeType = "application/json";

    @Value("${server.rootUrl}")
    private String rootUrl;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void rootResponseIsCorrect() throws IOException {
        HttpUriRequest request = new HttpGet( rootUrl);
        HttpResponse response = HttpClientBuilder.create().build().execute( request );

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        assertEquals( jsonMimeType, mimeType );

        // Check Response
        JsonNode root = mapper.readTree(response.getEntity().getContent());
        Assert.assertTrue(root.hasNonNull("value"));

        //TODO: expand tests to actually be useful
    }

}

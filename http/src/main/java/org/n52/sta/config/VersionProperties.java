package org.n52.sta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VersionProperties {

    @Value("${project.version}")
    private String projectVersion;

    @Value("${git.build.time}")
    private String buildTime;

    @Value("${git.remote.origin.url}")
    private String repository;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id.full}")
    private String commitId;

    @Value("${git.commit.time}")
    private String commitTime;

    @Value("${git.commit.message.short}")
    private String commitMessage;

    public ObjectNode getVersionInformation(ObjectMapper mapper) {
        Map<String, String> result = new HashMap<>();
        // TODO make this configurable
        result.put("project.name", "52North SensorThingsAPI");
//        result.put("project.version", buildProperties.getVersion());
//        result.put("project.time", buildProperties.getTime().toString());
        result.put("git.builddate", buildTime);
        result.put("git.repository", repository);
        result.put("git.path", branch);
        result.put("git.revision", commitId);
        result.put("git.lastCommitMessage", commitMessage);
        result.put("git.lastCommitDate", commitTime);
        ObjectNode json = mapper.createObjectNode();
        result.forEach(json::put);
        return json;
    }
}

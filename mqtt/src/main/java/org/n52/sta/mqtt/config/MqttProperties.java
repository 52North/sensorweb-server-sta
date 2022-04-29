package org.n52.sta.mqtt.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server.feature.mqtt")
public class MqttProperties {

    // TODO move all server.feature.mqtt.* properties to here

    private boolean enabled;
    private boolean readOnly;

    private boolean brokerTcpEnabled;
    private String brokerTcpHost;
    private int brokerTcpPort;

    private boolean brokerWsEnabled;
    private String brokerWsHost;
    private int brokerWsPort;

    private Set<String> publicationTopics;

    private String storeFolder;
    private String brokerStoreFilename;
    private String brokerStoreAutosaveInSeconds;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isBrokerTcpEnabled() {
        return brokerTcpEnabled;
    }

    public void setBrokerTcpEnabled(boolean brokerTcpEnabled) {
        this.brokerTcpEnabled = brokerTcpEnabled;
    }

    public String getBrokerTcpHost() {
        return brokerTcpHost;
    }

    public void setBrokerTcpHost(String brokerTcpHost) {
        this.brokerTcpHost = brokerTcpHost;
    }

    public int getBrokerTcpPort() {
        return brokerTcpPort;
    }

    public void setBrokerTcpPort(int brokerTcpPort) {
        this.brokerTcpPort = brokerTcpPort;
    }

    public boolean isBrokerWsEnabled() {
        return brokerWsEnabled;
    }

    public void setBrokerWsEnabled(boolean brokerWsEnabled) {
        this.brokerWsEnabled = brokerWsEnabled;
    }

    public String getBrokerWsHost() {
        return brokerWsHost;
    }

    public void setBrokerWsHost(String brokerWsHost) {
        this.brokerWsHost = brokerWsHost;
    }

    public int getBrokerWsPort() {
        return brokerWsPort;
    }

    public void setBrokerWsPort(int brokerWsPort) {
        this.brokerWsPort = brokerWsPort;
    }

    public Set<String> getPublicationTopics() {
        return publicationTopics;
    }

    public void setPublicationTopics(Set<String> publicationTopics) {
        this.publicationTopics = publicationTopics;
    }

    public String getStoreFolder() {
        return storeFolder;
    }

    public void setStoreFolder(String storeFolder) {
        this.storeFolder = storeFolder;
    }

    public String getBrokerStoreFilename() {
        return brokerStoreFilename;
    }

    public void setBrokerStoreFilename(String brokerStoreFilename) {
        this.brokerStoreFilename = brokerStoreFilename;
    }

    public String getBrokerStoreAutosaveInSeconds() {
        return brokerStoreAutosaveInSeconds;
    }

    public void setBrokerStoreAutosaveInSeconds(String brokerStoreAutosaveInSeconds) {
        this.brokerStoreAutosaveInSeconds = brokerStoreAutosaveInSeconds;
    }

}

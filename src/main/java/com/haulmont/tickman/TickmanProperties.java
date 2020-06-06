package com.haulmont.tickman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "tickman")
@ConstructorBinding
public class TickmanProperties {

    private static final Logger log = LoggerFactory.getLogger(TickmanProperties.class);

    private final String organization;
    private final String repo;
    private final int repoId;
    private final String zenHubToken; // TICKMAN_ZENHUBTOKEN

    public TickmanProperties(String organization, String repo, int repoId, String zenHubToken, Map<String, List<String>> teams) {
        this.organization = organization;
        this.repo = repo;
        this.repoId = repoId;
        this.zenHubToken = zenHubToken;
    }

    public String getOrganization() {
        return organization;
    }

    public String getRepo() {
        return repo;
    }

    public int getRepoId() {
        return repoId;
    }

    public String getZenHubToken() {
        return zenHubToken;
    }
}

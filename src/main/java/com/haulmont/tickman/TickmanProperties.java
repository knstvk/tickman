package com.haulmont.tickman;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "tickman")
@ConstructorBinding
public class TickmanProperties {

    private final String zenHubToken; // TICKMAN_ZENHUBTOKEN
    private final String gitHubToken; // TICKMAN_GITHUBTOKEN
    private final List<Integer> estimateValues;
    private final List<String> pipelines;

    public TickmanProperties(String organization,
                             String repo,
                             int repoId,
                             String zenHubToken,
                             String gitHubToken,
                             List<String> estimateValues,
                             List<String> pipelines) {
        this.zenHubToken = zenHubToken;
        this.gitHubToken = gitHubToken;
        this.estimateValues = estimateValues != null ? estimateValues.stream().map(Integer::valueOf).collect(Collectors.toList()) : null;
        this.pipelines = pipelines;
    }

    public String getZenHubToken() {
        return zenHubToken;
    }

    public String getGitHubToken() {
        return gitHubToken;
    }

    public List<Integer> getEstimateValues() {
        return estimateValues;
    }

    public List<String> getPipelines() {
        return pipelines;
    }
}

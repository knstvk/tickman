package com.haulmont.tickman.retrofit;

import java.util.List;

public class GitHubIssue {

    private Integer number;
    private String createdAt;
    private String title;
    private String body;
    private String htmlUrl;
    private List<GitHubLabel> labels;
    private GitHubAssignee assignee;
    private GitHubMilestone milestone;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public List<GitHubLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<GitHubLabel> labels) {
        this.labels = labels;
    }

    public GitHubAssignee getAssignee() {
        return assignee;
    }

    public void setAssignee(GitHubAssignee assignee) {
        this.assignee = assignee;
    }

    public GitHubMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(GitHubMilestone milestone) {
        this.milestone = milestone;
    }
}

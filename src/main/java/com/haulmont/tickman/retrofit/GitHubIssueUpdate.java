package com.haulmont.tickman.retrofit;

import java.util.List;

public class GitHubIssueUpdate {

    private List<String> assignees;
    private int milestone;

    public List<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }

    public int getMilestone() {
        return milestone;
    }

    public void setMilestone(int milestone) {
        this.milestone = milestone;
    }
}

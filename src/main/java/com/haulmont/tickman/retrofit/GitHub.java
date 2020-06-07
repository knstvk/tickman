package com.haulmont.tickman.retrofit;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface GitHub {

    @GET("repos/{organization}/{repo}/milestones")
    Call<List<GitHubMilestone>> loadMilestones(
            @Path("organization") String organization,
            @Path("repo") String repo,
            @Query("page") int page
    );

    @GET("repos/{organization}/{repo}/assignees")
    Call<List<GitHubAssignee>> loadAssignees(
            @Path("organization") String organization,
            @Path("repo") String repo,
            @Query("page") int page
    );

    @GET("repos/{organization}/{repo}/issues")
    Call<List<GitHubIssue>> loadIssues(
            @Path("organization") String organization,
            @Path("repo") String repo,
            @Query("state") String state,
            @Query("page") int page
    );

    @PATCH("/repos/{organization}/{repo}/issues/{issue}")
    Call<GitHubIssue> updateIssue(
            @Path("organization") String organization,
            @Path("repo") String repo,
            @Path("issue") int issue,
            @Body GitHubIssueUpdate update
    );
}

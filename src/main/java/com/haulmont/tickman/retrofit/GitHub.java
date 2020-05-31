package com.haulmont.tickman.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface GitHub {

    @GET("repos/{organization}/{repo}/issues")
    Call<List<GitHubIssue>> listIssues(
            @Path("organization") String organization,
            @Path("repo") String repo,
            @Query("state") String state,
            @Query("page") int page
    );
}

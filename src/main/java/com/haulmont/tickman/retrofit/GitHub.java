package com.haulmont.tickman.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface GitHub {

    @GET("repos/{organization}/{repo}/issues?state=open")
    Call<List<GitHubIssue>> listIssues(@Path("organization") String organization, @Path("repo") String repo);
}

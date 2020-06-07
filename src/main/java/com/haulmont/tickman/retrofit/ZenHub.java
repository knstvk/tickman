package com.haulmont.tickman.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ZenHub {

    @GET("/p1/repositories/{repoId}/issues/{issue}")
    Call<ZenHubIssue> getIssue(@Path("repoId") int repoId, @Path("issue") int issue);

    @PUT("/p1/repositories/{repoId}/issues/{issue}/estimate")
    Call<ZenHubEstimateUpdate> setEstimate(@Path("repoId") int repoId, @Path("issue") int issue, @Body ZenHubEstimateUpdate update);
}

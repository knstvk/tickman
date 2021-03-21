package com.haulmont.tickman.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.haulmont.tickman.TickmanProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Component
public class RetrofitBuilders {

    @Autowired
    private TickmanProperties properties;

    public Retrofit.Builder githubBuilder() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
                    .header("Authorization", "token " + properties.getGitHubToken())
                    .build();
            return chain.proceed(newRequest);
        }).build();

        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()));
    }

    public Retrofit.Builder zenhubBuilder() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addInterceptor(chain -> {
            Request newRequest = chain.request().newBuilder()
                    .header("X-Authentication-Token", properties.getZenHubToken())
                    .build();
            return chain.proceed(newRequest);
        }).build();

        return new Retrofit.Builder()
                .baseUrl("https://api.zenhub.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()));
    }
}

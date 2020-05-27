package com.haulmont.tickman.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.retrofit.*;
import io.jmix.core.DataManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TickmanProperties properties;

    @Autowired
    private DataManager dataManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteTickets() {
        entityManager.createQuery("delete from tickman_Ticket t").executeUpdate();
    }

    public List<Ticket> loadTickets() {
        List<GitHubIssue> githubIssues;

        GitHub gitHub = githubBuilder().build().create(GitHub.class);
        ZenHub zenHub = zenhubBuilder().build().create(ZenHub.class);

        log.info("Loading GitHub issues");
        Call<List<GitHubIssue>> issuesCall = gitHub.listIssues(properties.getOrganization(), properties.getRepo());
        try {
            Response<List<GitHubIssue>> response = issuesCall.execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unsuccessful response: " + response);
            }
            githubIssues = response.body();
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + issuesCall.request().url(), e);
        }

        if (githubIssues != null) {
            return githubIssues.stream()
                    .map(issue -> {
                        Ticket ticket = dataManager.create(Ticket.class);
                        ticket.setNum(issue.getNumber());
                        ticket.setHtmlUrl(issue.getHtmlUrl());
                        ticket.setTitle(issue.getTitle());
                        ticket.setDescription(issue.getBody());
                        ticket.setEstimate(loadEstimate(zenHub, issue.getNumber()));
                        dataManager.save(ticket);
                        return ticket;
                    })
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Integer loadEstimate(ZenHub zenHub, Integer number) {
        log.info("Loading ZenHub issue " + number);
        Call<ZenHubIssue> issueCall = zenHub.getIssue(properties.getRepoId(), number);
        try {
            Response<ZenHubIssue> response = issueCall.execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unsuccessful response: " + response);
            }
            ZenHubIssue issue = response.body();
            if (issue != null) {
                ZenHubEstimate estimate = issue.getEstimate();
                if (estimate != null) {
                    return estimate.getValue();
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + issueCall.request().url(), e);
        }
    }

    private Retrofit.Builder githubBuilder() {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()));
    }

    private Retrofit.Builder zenhubBuilder() {
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

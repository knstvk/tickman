package com.haulmont.tickman.service;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Team;
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

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public List<Ticket> loadTicketsFromGitHub() {
        List<GitHubIssue> githubIssues = new ArrayList<>();

        GitHub gitHub = githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub issues, page " + page);
            Call<List<GitHubIssue>> issuesCall = gitHub.listIssues(properties.getOrganization(), properties.getRepo(), "open", page);
            try {
                Response<List<GitHubIssue>> response = issuesCall.execute();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Unsuccessful response: " + response);
                }
                if (response.body() != null) {
                    githubIssues.addAll(response.body());

                    String link = response.headers().get("Link");
                    if (link != null && link.contains("rel=\"next\"")) {
                        page++;
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error accessing " + issuesCall.request().url(), e);
            }
        }

        List<Team> teams = dataManager.load(Team.class).list();

        return githubIssues.stream()
                .map(issue -> {
                    Ticket ticket = dataManager.create(Ticket.class);
                    ticket.setNum(issue.getNumber());
                    ticket.setHtmlUrl(issue.getHtmlUrl());
                    ticket.setTitle(issue.getTitle());
                    ticket.setDescription(issue.getBody());
                    ticket.setMilestone(issue.getMilestone() != null ? issue.getMilestone().getTitle() : null);
                    ticket.setAssignee(issue.getAssignee() != null ? issue.getAssignee().getLogin() : null);
                    if (ticket.getAssignee() != null) {
                        ticket.setTeam(selectTeam(teams, ticket.getAssignee()));
                    }

                    String labels = null;
                    List<GitHubLabel> labelList = issue.getLabels();
                    if (labelList != null) {
                        labels = labelList.stream().map(GitHubLabel::getName).collect(Collectors.joining(", "));
                    }
                    ticket.setLabels(labels);

                    return dataManager.save(ticket);
                })
                .collect(Collectors.toList());
    }

    @Nullable
    private Team selectTeam(List<Team> teams, String assignee) {
        return teams.stream()
                .filter(team -> team.getMembers().contains(assignee))
                .findAny()
                .orElse(null);
    }

    public List<Ticket> updateTicketsFromZenHub(List<Ticket> tickets) {
        List<Ticket> resultList = new ArrayList<>();
        ZenHub zenHub = zenhubBuilder().build().create(ZenHub.class);

        for (Ticket ticket : tickets) {
            long resetSec = loadZenHubInfo(zenHub, ticket);
            resultList.add(dataManager.save(ticket));
            long waitSec = resetSec - Instant.now().getEpochSecond();
            if (waitSec >= 0) {
                log.info("Sleeping for " + (waitSec + 1) + " sec to satisfy ZenHub rate limit");
                try {
                    Thread.sleep((waitSec + 1) * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return resultList;
    }

    public long loadZenHubInfo(@Nullable ZenHub zenHub, Ticket ticket) {
        log.info("Loading ZenHub issue " + ticket.getNum());
        if (zenHub == null) {
            zenHub = zenhubBuilder().build().create(ZenHub.class);
        }
        Call<ZenHubIssue> issueCall = zenHub.getIssue(properties.getRepoId(), ticket.getNum());
        try {
            Response<ZenHubIssue> response = issueCall.execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Unsuccessful response: " + response);
            }
            ZenHubIssue issue = response.body();
            ticket.setEstimate(getEstimate(issue));
            ticket.setEpic(issue.isEpic());

            long rateLimit = getHeaderLongValue(response, "X-RateLimit-Limit");
            long rateLimitUsed = getHeaderLongValue(response, "X-RateLimit-Used");
            if (rateLimit - rateLimitUsed <= 0) {
                return getHeaderLongValue(response, "X-RateLimit-Reset");
            } else {
                return 0;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + issueCall.request().url(), e);
        }
    }

    private long getHeaderLongValue(Response<?> response, String name) {
        String str = response.headers().get(name);
        if (str == null)
            throw new RuntimeException("Header " + name + " not set");
        return Long.parseLong(str);
    }

    private Integer getEstimate(ZenHubIssue issue) {
        if (issue != null) {
            ZenHubEstimate estimate = issue.getEstimate();
            if (estimate != null) {
                return estimate.getValue();
            }
        }
        return null;
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

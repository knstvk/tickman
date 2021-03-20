package com.haulmont.tickman.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.haulmont.tickman.TickmanProperties;
import com.haulmont.tickman.entity.Assignee;
import com.haulmont.tickman.entity.Milestone;
import com.haulmont.tickman.entity.Team;
import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.retrofit.*;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        entityManager.createQuery("delete from tickman_Milestone m").executeUpdate();
        entityManager.createQuery("delete from tickman_Assignee a").executeUpdate();
    }

    public List<Ticket> loadIssues(List<Assignee> assignees, List<Milestone> milestones) {
        List<GitHubIssue> githubIssues = new ArrayList<>();

        GitHub gitHub = githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub issues, page " + page);
            Call<List<GitHubIssue>> issuesCall = gitHub.loadIssues(properties.getOrganization(), properties.getRepo(), "open", page);
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

        return githubIssues.stream()
                .map(issue -> {
                    Ticket ticket = dataManager.create(Ticket.class);
                    ticket.setNum(issue.getNumber());
                    ticket.setCreatedAt(LocalDate.parse(issue.getCreatedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    ticket.setHtmlUrl(issue.getHtmlUrl());
                    ticket.setTitle(issue.getTitle());
                    ticket.setDescription(issue.getBody());
                    ticket.setMilestone(getMilestone(issue, milestones));
                    ticket.setAssignee(getAssignee(issue, assignees));

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
    private Milestone getMilestone(GitHubIssue issue, List<Milestone> milestones) {
        if (issue.getMilestone() == null) {
            return null;
        }
        return milestones.stream()
                .filter(milestone -> milestone.getNumber().equals(issue.getMilestone().getNumber()))
                .findAny()
                .orElse(null);
    }

    @Nullable
    private Assignee getAssignee(GitHubIssue issue, List<Assignee> assignees) {
        if (issue.getAssignee() == null) {
            return null;
        }
        return assignees.stream()
                .filter(assignee -> assignee.getLogin().equals(issue.getAssignee().getLogin()))
                .findAny()
                .orElse(null);
    }

    public List<Milestone> loadMilestones() {
        List<GitHubMilestone> githubMilestones = new ArrayList<>();

        GitHub gitHub = githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub milestones, page " + page);
            Call<List<GitHubMilestone>> call = gitHub.loadMilestones(properties.getOrganization(), properties.getRepo(), page);
            try {
                Response<List<GitHubMilestone>> response = call.execute();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Unsuccessful response: " + response);
                }
                if (response.body() != null) {
                    githubMilestones.addAll(response.body());

                    String link = response.headers().get("Link");
                    if (link != null && link.contains("rel=\"next\"")) {
                        page++;
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error accessing " + call.request().url(), e);
            }
        }

        return githubMilestones.stream()
                .map(gitHubMilestone -> {
                    Milestone milestone = dataManager.create(Milestone.class);
                    milestone.setNumber(gitHubMilestone.getNumber());
                    milestone.setTitle(gitHubMilestone.getTitle());
                    return dataManager.save(milestone);
                })
                .collect(Collectors.toList());
    }

    public List<Assignee> loadAssignees() {
        List<GitHubAssignee> githubAssignees = new ArrayList<>();

        GitHub gitHub = githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub assignees, page " + page);
            Call<List<GitHubAssignee>> call = gitHub.loadAssignees(properties.getOrganization(), properties.getRepo(), page);
            try {
                Response<List<GitHubAssignee>> response = call.execute();
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Unsuccessful response: " + response);
                }
                if (response.body() != null) {
                    githubAssignees.addAll(response.body());

                    String link = response.headers().get("Link");
                    if (link != null && link.contains("rel=\"next\"")) {
                        page++;
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error accessing " + call.request().url(), e);
            }
        }

        List<Team> teams = dataManager.load(Team.class).all().list();

        return githubAssignees.stream()
                .map(gitHubAssignee -> {
                    Team team = teams.stream()
                            .filter(t -> t.getMembers().contains(gitHubAssignee.getLogin()))
                            .findAny()
                            .orElse(null);
                    Assignee assignee = dataManager.create(Assignee.class);
                    assignee.setLogin(gitHubAssignee.getLogin());
                    assignee.setTeam(team);
                    return dataManager.save(assignee);
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
            ticket.setPipeline(issue.getPipeline() != null ? issue.getPipeline().getName() : null);

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

    public void updateTicket(Ticket ticket) {
        Ticket oldTicket = dataManager.load(Id.of(ticket)).fetchPlan(fp -> fp.addAll("estimate", "assignee", "milestone")).one();

        if (!Objects.equals(oldTicket.getEstimate(), ticket.getEstimate())) {
            updateOnZenHub(ticket);
        }

        if (!Objects.equals(oldTicket.getAssignee(), ticket.getAssignee())
                || !Objects.equals(oldTicket.getMilestone(), ticket.getMilestone())) {
            updateOnGitHub(ticket);
        }
    }

    private void updateOnZenHub(Ticket ticket) {
        log.info("Updating ZenHub estimate " + ticket.getNum());
        ZenHub zenHub = zenhubBuilder().build().create(ZenHub.class);
        ZenHubEstimateUpdate update = new ZenHubEstimateUpdate();
        update.setEstimate(ticket.getEstimate());
        Call<ZenHubEstimateUpdate> call = zenHub.setEstimate(properties.getRepoId(), ticket.getNum(), update);
        try {
            Response<ZenHubEstimateUpdate> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Unsuccessful response: " + response);
            }
            ZenHubEstimateUpdate estimate = response.body();
            if (!estimate.getEstimate().equals(ticket.getEstimate())) {
                throw new RuntimeException("ZenHub update failed");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + call.request().url(), e);
        }
    }

    private void updateOnGitHub(Ticket ticket) {
        log.info("Updating GitHub issue " + ticket.getNum());
        GitHub gitHub = githubBuilder().build().create(GitHub.class);
        GitHubIssueUpdate update = new GitHubIssueUpdate();
        update.setAssignees(Collections.singletonList(ticket.getAssignee().getLogin()));
        update.setMilestone(ticket.getMilestone().getNumber());
        Call<GitHubIssue> call = gitHub.updateIssue(properties.getOrganization(), properties.getRepo(), ticket.getNum(), update);
        try {
            Response<GitHubIssue> response = call.execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Unsuccessful response: " + response
                        + (response.errorBody() != null ? "\n" + response.errorBody().string() : ""));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + call.request().url(), e);
        }
    }

    private Retrofit.Builder githubBuilder() {
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

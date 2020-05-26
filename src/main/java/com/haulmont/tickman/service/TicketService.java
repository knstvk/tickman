package com.haulmont.tickman.service;

import com.haulmont.tickman.entity.Ticket;
import com.haulmont.tickman.retrofit.GitHub;
import com.haulmont.tickman.retrofit.GitHubIssue;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketService {

    public List<Ticket> loadTickets() {
        List<GitHubIssue> githubIssues;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHub github = retrofit.create(GitHub.class);
        Call<List<GitHubIssue>> issuesCall = github.listIssues("jmix-framework", "jmix");
        try {
            Response<List<GitHubIssue>> response = issuesCall.execute();
            githubIssues = response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (githubIssues != null) {
            return githubIssues.stream()
                    .map(issue -> {
                        Ticket ticket = new Ticket();
                        ticket.setId(issue.getNumber());
                        ticket.setTitle(issue.getTitle());
                        ticket.setDescription(issue.getBody());
                        return ticket;
                    })
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}

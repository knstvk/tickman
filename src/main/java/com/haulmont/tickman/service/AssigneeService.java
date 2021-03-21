package com.haulmont.tickman.service;

import com.haulmont.tickman.entity.Assignee;
import com.haulmont.tickman.entity.Repository;
import com.haulmont.tickman.entity.Team;
import com.haulmont.tickman.retrofit.GitHub;
import com.haulmont.tickman.retrofit.GitHubAssignee;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AssigneeService {

    private static final Logger log = LoggerFactory.getLogger(AssigneeService.class);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private RetrofitBuilders retrofitBuilders;

    public List<Assignee> loadAssignees(Repository repository) {
        List<GitHubAssignee> githubAssignees = new ArrayList<>();

        GitHub gitHub = retrofitBuilders.githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub assignees from {}, page {}", repository.getName(), page);
            Call<List<GitHubAssignee>> call = gitHub.loadAssignees(repository.getOrgName(), repository.getRepoName(), page);
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
                .filter(gitHubAssignee -> {
                    return !dataManager.load(Assignee.class)
                            .condition(PropertyCondition.equal("login", gitHubAssignee.getLogin()))
                            .optional()
                            .isPresent();
                })
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

}

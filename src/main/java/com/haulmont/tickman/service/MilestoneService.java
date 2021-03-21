package com.haulmont.tickman.service;

import com.haulmont.tickman.entity.Milestone;
import com.haulmont.tickman.entity.Repository;
import com.haulmont.tickman.retrofit.GitHub;
import com.haulmont.tickman.retrofit.GitHubMilestone;
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
public class MilestoneService {

    private static final Logger log = LoggerFactory.getLogger(MilestoneService.class);

    @Autowired
    private DataManager dataManager;

    @Autowired
    private RetrofitBuilders retrofitBuilders;

    public List<Milestone> loadMilestones(Repository repository) {
        List<GitHubMilestone> githubMilestones = new ArrayList<>();

        GitHub gitHub = retrofitBuilders.githubBuilder().build().create(GitHub.class);

        int page = 1;
        while (true) {
            log.info("Loading GitHub milestones from {}, page {}", repository.getName(), page);
            Call<List<GitHubMilestone>> call = gitHub.loadMilestones(repository.getOrgName(), repository.getRepoName(), page);
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
                .filter(gitHubMilestone -> {
                    return !dataManager.load(Milestone.class)
                            .condition(PropertyCondition.equal("title", gitHubMilestone.getTitle()))
                            .optional()
                            .isPresent();

                })
                .map(gitHubMilestone -> {
                    Milestone milestone = dataManager.create(Milestone.class);
                    milestone.setNumber(gitHubMilestone.getNumber());
                    milestone.setTitle(gitHubMilestone.getTitle());
                    return dataManager.save(milestone);
                })
                .collect(Collectors.toList());
    }

}

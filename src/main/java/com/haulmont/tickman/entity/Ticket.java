package com.haulmont.tickman.entity;

import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.annotation.InstanceName;

import javax.persistence.*;

@Entity(name = "tickman_Ticket")
@Table(name = "TICKMAN_TICKET")
public class Ticket implements io.jmix.core.Entity, Versioned {

    @Id
    @Column(name = "NUM", nullable = false)
    private Integer num;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "HTML_URL")
    private String htmlUrl;

    @Column(name = "TITLE")
    @InstanceName
    private String title;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ESTIMATE")
    private Integer estimate;

    @Column(name = "MILESTONE")
    private String milestone;

    @Column(name = "ASSIGNEE")
    private String assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    @Column(name = "LABELS")
    private String labels;

    @Column(name = "EPIC")
    private Boolean epic;

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Boolean getEpic() {
        return epic;
    }

    public void setEpic(Boolean epic) {
        this.epic = epic;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getEstimate() {
        return estimate;
    }

    public void setEstimate(Integer estimate) {
        this.estimate = estimate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
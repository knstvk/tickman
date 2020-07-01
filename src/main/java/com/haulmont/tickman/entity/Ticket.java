package com.haulmont.tickman.entity;

import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.annotation.InstanceName;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "tickman_Ticket")
@Table(name = "TICKMAN_TICKET")
public class Ticket implements io.jmix.core.Entity, Versioned {

    @Id
    @Column(name = "NUM", nullable = false)
    private Integer num;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "CREATED_AT")
    private LocalDate createdAt;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MILESTONE_ID")
    private Milestone milestone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNEE_ID")
    private Assignee assignee;

    @Column(name = "PIPELINE")
    private String pipeline;

    @Column(name = "LABELS")
    private String labels;

    @Column(name = "EPIC")
    private Boolean epic;

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    public Assignee getAssignee() {
        return assignee;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
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
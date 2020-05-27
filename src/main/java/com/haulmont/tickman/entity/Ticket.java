package com.haulmont.tickman.entity;

import io.jmix.core.metamodel.annotation.InstanceName;

import javax.persistence.*;

@Entity(name = "tickman_Ticket")
@Table(name = "TICKMAN_TICKET")
public class Ticket implements io.jmix.core.Entity {
    private static final long serialVersionUID = 6942806518596867842L;

    @Id
    @Column(name = "NUM", nullable = false)
    private Integer num;

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
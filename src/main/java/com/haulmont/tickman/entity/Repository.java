package com.haulmont.tickman.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@JmixEntity
@Table(name = "TICKMAN_REPOSITORY")
@Entity(name = "tickman_Repository")
public class Repository {

    @Column(name = "ID", nullable = false)
    @Id
    private Integer id;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @Column(name = "ORG_NAME", nullable = false)
    @NotNull
    private String orgName;

    @Column(name = "REPO_NAME", nullable = false)
    @NotNull
    private String repoName;

    @InstanceName
    @DependsOnProperties({"orgName", "repoName"})
    public String getName() {
        return orgName + "/" + repoName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String org) {
        this.orgName = org;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repository) {
        this.repoName = repository;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
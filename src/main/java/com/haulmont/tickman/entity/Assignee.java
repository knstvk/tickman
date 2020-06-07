package com.haulmont.tickman.entity;

import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.annotation.InstanceName;

import javax.persistence.*;

@Table(name = "TICKMAN_ASSIGNEE")
@Entity(name = "tickman_Assignee")
public class Assignee implements io.jmix.core.Entity, Versioned {

    @Id
    @Column(name = "LOGIN", nullable = false)
    @InstanceName
    private String login;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }
}
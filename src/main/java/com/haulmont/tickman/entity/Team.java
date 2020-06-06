package com.haulmont.tickman.entity;

import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.data.entity.BaseIntegerIdEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Table(name = "TICKMAN_TEAM")
@Entity(name = "tickman_Team")
public class Team extends BaseIntegerIdEntity implements Versioned {

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @NotNull
    @Column(name = "NAME", nullable = false)
    @InstanceName
    private String name;

    @Column(name = "MEMBERS")
    private String members;

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
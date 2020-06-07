package com.haulmont.tickman.entity;

import io.jmix.core.entity.Versioned;
import io.jmix.core.metamodel.annotation.InstanceName;

import javax.persistence.*;

@Table(name = "TICKMAN_MILESTONE")
@Entity(name = "tickman_Milestone")
public class Milestone implements io.jmix.core.Entity, Versioned {

    @Id
    @Column(name = "NUM", nullable = false)
    private Integer number;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "TITLE")
    @InstanceName
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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
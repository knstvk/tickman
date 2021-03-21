package com.haulmont.tickman.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Table(name = "TICKMAN_MILESTONE")
@Entity(name = "tickman_Milestone")
public class Milestone {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Column(name = "NUM", nullable = false)
    private Integer number;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "TITLE")
    @InstanceName
    private String title;

    @NotNull
    @JoinColumn(name = "REPOSITORY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Repository repository;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
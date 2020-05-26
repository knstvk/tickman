package com.haulmont.tickman.entity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.data.entity.BaseIntegerIdEntity;

@ModelObject(name = "tickman_Ticket")
public class Ticket extends BaseIntegerIdEntity {
    private static final long serialVersionUID = 6942806518596867842L;

    @ModelProperty
    @InstanceName
    private String title;

    @ModelProperty
    private String description;

    @ModelProperty
    private Integer estimate;

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
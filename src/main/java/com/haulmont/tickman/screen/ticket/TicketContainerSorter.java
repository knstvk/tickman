package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.TickmanProperties;
import io.jmix.core.Entity;
import io.jmix.core.Sort;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.impl.CollectionContainerSorter;

import java.util.Comparator;

public class TicketContainerSorter extends CollectionContainerSorter {

    private final TickmanProperties properties;

    public TicketContainerSorter(CollectionContainer container, TickmanProperties properties) {
        super(container, null);
        this.properties = properties;
    }

    @Override
    public void sort(Sort sort) {
        sortInMemory(sort);
    }

    @Override
    protected Comparator<? extends Entity> createComparator(Sort sort, MetaClass metaClass) {
        String property = sort.getOrders().get(0).getProperty();
        if (property.equals("pipeline")) {
            boolean asc = sort.getOrders().get(0).getDirection() == Sort.Direction.ASC;
            return Comparator.comparing(e -> EntityValues.getValue(e, "pipeline"), new PipelineComparator(asc, properties));

        } else {
            return super.createComparator(sort, metaClass);
        }
    }
}

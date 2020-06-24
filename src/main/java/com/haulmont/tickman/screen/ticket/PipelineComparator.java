package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.TickmanProperties;

import java.util.Comparator;
import java.util.List;

public class PipelineComparator implements Comparator<String> {

    private boolean asc;
    private TickmanProperties properties;

    public PipelineComparator(boolean asc, TickmanProperties properties) {
        this.asc = asc;
        this.properties = properties;
    }

    @Override
    public int compare(String p1, String p2) {
        List<String> pipelines = properties.getPipelines();
        int i = pipelines.indexOf(p1) - pipelines.indexOf(p2);
        return asc ? i : -i;
    }
}

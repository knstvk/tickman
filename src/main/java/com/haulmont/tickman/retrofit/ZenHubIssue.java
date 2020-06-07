package com.haulmont.tickman.retrofit;

public class ZenHubIssue {

    private ZenHubEstimate estimate;
    private boolean isEpic;
    private ZenHubPipeline pipeline;

    public ZenHubEstimate getEstimate() {
        return estimate;
    }

    public void setEstimate(ZenHubEstimate estimate) {
        this.estimate = estimate;
    }

    public boolean isEpic() {
        return isEpic;
    }

    public void setEpic(boolean epic) {
        isEpic = epic;
    }

    public ZenHubPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(ZenHubPipeline pipeline) {
        this.pipeline = pipeline;
    }
}

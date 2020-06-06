package com.haulmont.tickman.retrofit;

public class ZenHubIssue {

    private ZenHubEstimate estimate;
    private boolean isEpic;

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
}

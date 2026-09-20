package com.maybank.appsense.core.filter;

import com.maybank.appsense.core.buffer.RingBuffer;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.GapMarkerRecord;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StreamItem;
import java.util.ArrayList;
import java.util.List;

/** Applies level then keyword filtering; scope handling is done by the caller through scopeStart. */
public final class FilterService {

    private volatile LevelFilter levelFilter = LevelFilter.ALL;
    private volatile String keyword = "";
    private volatile boolean sinceLastDeployment;

    public LevelFilter getLevelFilter() {
        return levelFilter;
    }

    public void setLevelFilter(LevelFilter levelFilter) {
        this.levelFilter = levelFilter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword.trim();
    }

    public boolean isSinceLastDeployment() {
        return sinceLastDeployment;
    }

    public void setSinceLastDeployment(boolean sinceLastDeployment) {
        this.sinceLastDeployment = sinceLastDeployment;
    }

    public boolean accepts(StreamItem item) {
        return switch (item) {
            case LogLineRecord line -> acceptsLine(line);
            case DeploymentMarkerRecord marker -> true;
            case GapMarkerRecord gap -> true;
        };
    }

    /**
     * Index of the latest deployment marker below the limit when scoped to it, otherwise the first
     * index.
     */
    public int scopeStart(RingBuffer<StreamItem> history, int limit) {
        if (!sinceLastDeployment) {
            return 0;
        }
        for (int index = limit - 1; index >= 0; index--) {
            if (history.get(index) instanceof DeploymentMarkerRecord) {
                return index;
            }
        }
        return 0;
    }

    public List<StreamItem> apply(RingBuffer<StreamItem> history, int from, int to) {
        List<StreamItem> visible = new ArrayList<>();
        for (int index = from; index < to; index++) {
            StreamItem item = history.get(index);
            if (accepts(item)) {
                visible.add(item);
            }
        }
        return visible;
    }

    private boolean acceptsLine(LogLineRecord line) {
        if (!levelFilter.accepts(line.level())) {
            return false;
        }
        String needle = keyword;
        if (needle.isEmpty()) {
            return true;
        }
        return ContainsIgnoreCaseUtil.contains(line.message(), needle)
                || ContainsIgnoreCaseUtil.contains(line.logger(), needle)
                || line.stackTrace().stream().anyMatch(text -> ContainsIgnoreCaseUtil.contains(text, needle));
    }
}

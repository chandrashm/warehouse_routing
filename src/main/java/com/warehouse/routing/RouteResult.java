package com.warehouse.routing;

import java.util.Collections;
import java.util.List;

public class RouteResult {
    private final int totalTravelTimeSeconds;
    private final List<String> fullPath;
    private final List<String> pickOrder;

    public RouteResult(int totalTravelTimeSeconds, List<String> fullPath, List<String> pickOrder) {
        this.totalTravelTimeSeconds = totalTravelTimeSeconds;
        this.fullPath = Collections.unmodifiableList(fullPath);
        this.pickOrder = Collections.unmodifiableList(pickOrder);
    }

    public int getTotalTravelTimeSeconds() {
        return totalTravelTimeSeconds;
    }

    public List<String> getFullPath() {
        return fullPath;
    }

    public List<String> getPickOrder() {
        return pickOrder;
    }

    @Override
    public String toString() {
        return String.format("Total Time: %ds | Pick Order: %s | Full Path: %s",
                totalTravelTimeSeconds, pickOrder, String.join(" -> ", fullPath));
    }
}

package com.warehouse.routing;

public class RoutingStrategyFactory {
    private static final int EXACT_DP_THRESHOLD = 15;

    public static RoutingStrategy getStrategy(int pickCount) {
        if (pickCount <= EXACT_DP_THRESHOLD) {
            return new ExactBitmaskRoutingStrategy();
        }
        return new GreedyNearestNeighborStrategy();
    }
}

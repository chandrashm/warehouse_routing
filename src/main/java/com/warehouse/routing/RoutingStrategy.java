package com.warehouse.routing;

import java.util.List;
import java.util.Map;

public interface RoutingStrategy {
    RouteResult findOptimalRoute(
            String startNode,
            List<String> pickNodes,
            String endNode,
            Map<String, ShortestPathFinder.ShortestPathTree> shortestPathTrees
    );
}

package com.warehouse.routing;

import java.util.*;

public class GreedyNearestNeighborStrategy implements RoutingStrategy {
    private static final int INF = 1_000_000_000;

    @Override
    public RouteResult findOptimalRoute(
            String startNode,
            List<String> pickNodes,
            String endNode,
            Map<String, ShortestPathFinder.ShortestPathTree> trees) {

        Set<String> remaining = new LinkedHashSet<>(pickNodes);
        List<String> pickOrder = new ArrayList<>();
        List<String> fullPath = new ArrayList<>(List.of(startNode));

        String current = startNode;
        int totalTime = 0;

        while (!remaining.isEmpty()) {
            String bestNext = null;
            int bestDist = INF;
            Map<String, Integer> distMap = trees.get(current).getDistances();

            for (String candidate : remaining) {
                int d = distMap.getOrDefault(candidate, INF);
                if (d < bestDist) {
                    bestDist = d;
                    bestNext = candidate;
                }
            }

            if (bestNext == null) {
                throw new RouteNotFoundException("Unreachable pick location from " + current);
            }

            totalTime += bestDist;
            List<String> segment = trees.get(current).reconstructPath(current, bestNext);
            fullPath.addAll(segment.subList(1, segment.size()));

            pickOrder.add(bestNext);
            remaining.remove(bestNext);
            current = bestNext;
        }

        int toEnd = trees.get(current).getDistances().getOrDefault(endNode, INF);
        if (toEnd >= INF) {
            throw new RouteNotFoundException("Cannot reach " + endNode + " from " + current);
        }
        totalTime += toEnd;
        List<String> endSegment = trees.get(current).reconstructPath(current, endNode);
        fullPath.addAll(endSegment.subList(1, endSegment.size()));

        return new RouteResult(totalTime, fullPath, pickOrder);
    }
}

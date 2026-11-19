package com.warehouse.routing;

import java.util.*;

public class ExactBitmaskRoutingStrategy implements RoutingStrategy {
    private static final int INF = 1_000_000_000;

    @Override
    public RouteResult findOptimalRoute(
            String startNode,
            List<String> pickNodes,
            String endNode,
            Map<String, ShortestPathFinder.ShortestPathTree> trees) {

        int k = pickNodes.size();
        if (k == 0) {
            int directDist = trees.get(startNode).getDistances().getOrDefault(endNode, INF);
            if (directDist >= INF) {
                throw new RouteNotFoundException("No path from " + startNode + " to " + endNode);
            }
            List<String> path = trees.get(startNode).reconstructPath(startNode, endNode);
            return new RouteResult(directDist, path, List.of());
        }

        int totalMasks = 1 << k;
        int[][] dp = new int[totalMasks][k];
        int[][] parent = new int[totalMasks][k];
        for (int[] row : dp) Arrays.fill(row, INF);
        for (int[] row : parent) Arrays.fill(row, -1);

        for (int i = 0; i < k; i++) {
            int dist = trees.get(startNode).getDistances().getOrDefault(pickNodes.get(i), INF);
            dp[1 << i][i] = dist;
        }

        for (int mask = 1; mask < totalMasks; mask++) {
            for (int i = 0; i < k; i++) {
                if ((mask & (1 << i)) == 0 || dp[mask][i] >= INF) continue;

                String fromNode = pickNodes.get(i);
                Map<String, Integer> distFromI = trees.get(fromNode).getDistances();

                for (int j = 0; j < k; j++) {
                    if ((mask & (1 << j)) != 0) continue;

                    String toNode = pickNodes.get(j);
                    int travel = distFromI.getOrDefault(toNode, INF);
                    if (travel < INF) {
                        int nextMask = mask | (1 << j);
                        if (dp[mask][i] + travel < dp[nextMask][j]) {
                            dp[nextMask][j] = dp[mask][i] + travel;
                            parent[nextMask][j] = i;
                        }
                    }
                }
            }
        }

        int fullMask = totalMasks - 1;
        int minTotalTime = INF;
        int lastPickIdx = -1;

        for (int i = 0; i < k; i++) {
            if (dp[fullMask][i] >= INF) continue;
            int toEnd = trees.get(pickNodes.get(i)).getDistances().getOrDefault(endNode, INF);
            if (toEnd < INF && dp[fullMask][i] + toEnd < minTotalTime) {
                minTotalTime = dp[fullMask][i] + toEnd;
                lastPickIdx = i;
            }
        }

        if (minTotalTime >= INF || lastPickIdx == -1) {
            throw new RouteNotFoundException("No valid route covers all pick locations and reaches " + endNode);
        }

        // Reconstruct pick order
        LinkedList<String> pickOrder = new LinkedList<>();
        int currMask = fullMask;
        int currIdx = lastPickIdx;
        while (currIdx != -1) {
            pickOrder.addFirst(pickNodes.get(currIdx));
            int prevIdx = parent[currMask][currIdx];
            currMask ^= (1 << currIdx);
            currIdx = prevIdx;
        }

        // Stitch full node-by-node path
        List<String> fullPath = stitchFullPath(startNode, pickOrder, endNode, trees);
        return new RouteResult(minTotalTime, fullPath, pickOrder);
    }

    private List<String> stitchFullPath(
            String startNode,
            List<String> pickOrder,
            String endNode,
            Map<String, ShortestPathFinder.ShortestPathTree> trees) {
        List<String> waypoints = new ArrayList<>();
        waypoints.add(startNode);
        waypoints.addAll(pickOrder);
        waypoints.add(endNode);

        List<String> fullPath = new ArrayList<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            String from = waypoints.get(i);
            String to = waypoints.get(i + 1);
            List<String> segment = trees.get(from).reconstructPath(from, to);
            if (i > 0 && !segment.isEmpty()) {
                segment = segment.subList(1, segment.size());
            }
            fullPath.addAll(segment);
        }
        return fullPath;
    }
}

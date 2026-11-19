package com.warehouse.routing;

import java.util.*;

public class DijkstraPathFinder implements ShortestPathFinder {
    private static class NodeDistance {
        final String node;
        final int distance;

        NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    @Override
    public ShortestPathTree computeShortestPaths(WarehouseGraph graph, String source) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));

        distances.put(source, 0);
        pq.offer(new NodeDistance(source, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.node;
            int distU = current.distance;

            if (distU > distances.getOrDefault(u, Integer.MAX_VALUE)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(u)) {
                String v = edge.getDestination();
                int newDist = distU + edge.getTravelTimeSeconds();
                if (newDist < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                    distances.put(v, newDist);
                    parents.put(v, u);
                    pq.offer(new NodeDistance(v, newDist));
                }
            }
        }
        return new ShortestPathTree(distances, parents);
    }
}

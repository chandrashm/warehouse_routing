package com.warehouse.routing;

import java.util.List;
import java.util.Map;

public interface ShortestPathFinder {
    class ShortestPathTree {
        private final Map<String, Integer> distances;
        private final Map<String, String> parents;

        public ShortestPathTree(Map<String, Integer> distances, Map<String, String> parents) {
            this.distances = distances;
            this.parents = parents;
        }

        public Map<String, Integer> getDistances() {
            return distances;
        }

        public List<String> reconstructPath(String source, String target) {
            if (!distances.containsKey(target)) {
                return List.of();
            }
            java.util.LinkedList<String> path = new java.util.LinkedList<>();
            String curr = target;
            while (curr != null) {
                path.addFirst(curr);
                if (curr.equals(source)) break;
                curr = parents.get(curr);
            }
            return path;
        }
    }

    ShortestPathTree computeShortestPaths(WarehouseGraph graph, String source);
}

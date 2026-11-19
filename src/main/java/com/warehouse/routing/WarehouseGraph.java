package com.warehouse.routing;

import java.util.*;

public class WarehouseGraph {
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public void addLocation(String nodeId) {
        validateNodeId(nodeId);
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    public void addCorridor(String from, String to, int travelTimeSeconds) {
        validateNodeId(from);
        validateNodeId(to);
        addLocation(from);
        addLocation(to);
        adjacencyList.get(from).add(new Edge(to, travelTimeSeconds));
    }

    public List<Edge> getNeighbors(String nodeId) {
        return Collections.unmodifiableList(adjacencyList.getOrDefault(nodeId, Collections.emptyList()));
    }

    public boolean containsLocation(String nodeId) {
        return nodeId != null && adjacencyList.containsKey(nodeId);
    }

    public Set<String> getAllLocations() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    private void validateNodeId(String nodeId) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Location ID cannot be null or empty.");
        }
    }
}

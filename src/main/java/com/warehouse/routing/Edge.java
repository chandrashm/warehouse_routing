package com.warehouse.routing;

import java.util.Objects;

public class Edge {
    private final String destination;
    private final int travelTimeSeconds;

    public Edge(String destination, int travelTimeSeconds) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be null or empty.");
        }
        if (travelTimeSeconds < 0) {
            throw new IllegalArgumentException("Travel time cannot be negative.");
        }
        this.destination = destination;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    public String getDestination() {
        return destination;
    }

    public int getTravelTimeSeconds() {
        return travelTimeSeconds;
    }

    @Override
    public String toString() {
        return "-> " + destination + " (" + travelTimeSeconds + "s)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge edge)) return false;
        return travelTimeSeconds == edge.travelTimeSeconds && Objects.equals(destination, edge.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, travelTimeSeconds);
    }
}

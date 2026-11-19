package com.warehouse.routing;

import java.util.*;

public class WarehouseRoutingEngine {
    private final WarehouseGraph graph;
    private final ShortestPathFinder pathFinder;
    private final RoutingStrategy routingStrategy;

    public WarehouseRoutingEngine(WarehouseGraph graph, ShortestPathFinder pathFinder, RoutingStrategy routingStrategy) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.pathFinder = Objects.requireNonNull(pathFinder, "PathFinder cannot be null");
        this.routingStrategy = Objects.requireNonNull(routingStrategy, "RoutingStrategy cannot be null");
    }

    public RouteResult calculateOptimalRoute(String startNode, List<String> rawPickNodes, String endNode) {
        validateInputs(startNode, rawPickNodes, endNode);

        Set<String> uniquePicks = new LinkedHashSet<>(rawPickNodes);
        uniquePicks.remove(startNode);
        uniquePicks.remove(endNode);
        List<String> pickNodes = new ArrayList<>(uniquePicks);

        Map<String, ShortestPathFinder.ShortestPathTree> trees = new HashMap<>();
        trees.put(startNode, pathFinder.computeShortestPaths(graph, startNode));
        for (String pick : pickNodes) {
            trees.put(pick, pathFinder.computeShortestPaths(graph, pick));
        }

        return routingStrategy.findOptimalRoute(startNode, pickNodes, endNode, trees);
    }

    public int calculateOptimalPickTime(String startNode, List<String> rawPickNodes, String endNode) {
        return calculateOptimalRoute(startNode, rawPickNodes, endNode).getTotalTravelTimeSeconds();
    }

    private void validateInputs(String startNode, List<String> pickNodes, String endNode) {
        if (pickNodes == null) {
            throw new IllegalArgumentException("Pick nodes list cannot be null.");
        }
        if (!graph.containsLocation(startNode)) {
            throw new IllegalArgumentException("Invalid start node: " + startNode);
        }
        if (!graph.containsLocation(endNode)) {
            throw new IllegalArgumentException("Invalid end node: " + endNode);
        }
        for (String pick : pickNodes) {
            if (!graph.containsLocation(pick)) {
                throw new IllegalArgumentException("Invalid pick node: " + pick);
            }
        }
    }

    public static void main(String[] args) {
        WarehouseGraph warehouse = new WarehouseGraph();
        warehouse.addCorridor("PACK", "S1", 10);
        warehouse.addCorridor("PACK", "S2", 25);
        warehouse.addCorridor("S1", "S2", 12);
        warehouse.addCorridor("S2", "S3", 8);
        warehouse.addCorridor("S3", "S1", 5);
        warehouse.addCorridor("S2", "DISPATCH", 20);
        warehouse.addCorridor("S3", "DISPATCH", 15);

        WarehouseRoutingEngine engine = new WarehouseRoutingEngine(
                warehouse,
                new DijkstraPathFinder(),
                new ExactBitmaskRoutingStrategy()
        );

        RouteResult result = engine.calculateOptimalRoute("PACK", Arrays.asList("S1", "S2", "S3"), "DISPATCH");
        System.out.println(result);
    }
}

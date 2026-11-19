package com.warehouse.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseRoutingEngineTest {

    private WarehouseGraph warehouse;
    private WarehouseRoutingEngine engine;

    @BeforeEach
    void setUp() {
        warehouse = new WarehouseGraph();
        warehouse.addCorridor("PACK", "S1", 10);
        warehouse.addCorridor("PACK", "S2", 25);
        warehouse.addCorridor("S1", "S2", 12);
        warehouse.addCorridor("S2", "S3", 8);
        warehouse.addCorridor("S3", "S1", 5);
        warehouse.addCorridor("S2", "DISPATCH", 20);
        warehouse.addCorridor("S3", "DISPATCH", 15);

        engine = new WarehouseRoutingEngine(
                warehouse,
                new DijkstraPathFinder(),
                new ExactBitmaskRoutingStrategy()
        );
    }

    @Test
    @DisplayName("Happy Path: Visits all pick nodes in optimal order and reaches DISPATCH")
    void testHappyPathMultiplePicks() {
        RouteResult result = engine.calculateOptimalRoute("PACK", Arrays.asList("S1", "S2", "S3"), "DISPATCH");
        assertEquals(45, result.getTotalTravelTimeSeconds());
        assertEquals(List.of("S1", "S2", "S3"), result.getPickOrder());
        assertEquals(List.of("PACK", "S1", "S2", "S3", "DISPATCH"), result.getFullPath());
    }

    @Test
    @DisplayName("Out-of-Order Picks: Chooses S3 -> S1 when S3 is faster to reach first")
    void testOutOfOrderPickSelection() {
        WarehouseGraph customGraph = new WarehouseGraph();
        customGraph.addCorridor("PACK", "S3", 5);
        customGraph.addCorridor("PACK", "S1", 30);
        customGraph.addCorridor("S3", "S1", 4);
        customGraph.addCorridor("S1", "DISPATCH", 6);
        customGraph.addCorridor("S3", "DISPATCH", 25);

        WarehouseRoutingEngine customEngine = new WarehouseRoutingEngine(
                customGraph, new DijkstraPathFinder(), new ExactBitmaskRoutingStrategy());

        RouteResult result = customEngine.calculateOptimalRoute("PACK", List.of("S1", "S3"), "DISPATCH");
        assertEquals(15, result.getTotalTravelTimeSeconds());
        assertEquals(List.of("S3", "S1"), result.getPickOrder());
    }

    @Test
    @DisplayName("Intermediate Pass-Through: Allows passing through unrequested shelf on shortest path")
    void testIntermediateNodePassThrough() {
        // S2 is not in the pick list, but PACK -> S1 -> S2 -> S3 -> DISPATCH is required
        RouteResult result = engine.calculateOptimalRoute("PACK", List.of("S1", "S3"), "DISPATCH");
        assertEquals(45, result.getTotalTravelTimeSeconds());
        assertEquals(List.of("PACK", "S1", "S2", "S3", "DISPATCH"), result.getFullPath());
    }

    @Test
    @DisplayName("Edge Case: Empty pick list goes directly from PACK to DISPATCH")
    void testEmptyPickList() {
        RouteResult result = engine.calculateOptimalRoute("PACK", Collections.emptyList(), "DISPATCH");
        // Shortest path PACK -> S1 (10) -> S2 (12) -> S3 (8) -> DISPATCH (15) = 45
        assertEquals(45, result.getTotalTravelTimeSeconds());
        assertTrue(result.getPickOrder().isEmpty());
    }

    @Test
    @DisplayName("Edge Case: Duplicate pick nodes and start/end included in pick list are deduplicated")
    void testDuplicateAndBoundaryPicks() {
        RouteResult result = engine.calculateOptimalRoute(
                "PACK", Arrays.asList("PACK", "S1", "S1", "S2", "DISPATCH"), "DISPATCH");
        // PACK -> S1 (10) -> S2 (12) -> DISPATCH (20) = 42
        assertEquals(42, result.getTotalTravelTimeSeconds());
        assertEquals(List.of("S1", "S2"), result.getPickOrder());
    }

    @Test
    @DisplayName("Disconnected Graph: Throws RouteNotFoundException when DISPATCH is unreachable")
    void testUnreachableDispatchThrowsException() {
        warehouse.addLocation("ISOLATED_DISPATCH");
        assertThrows(RouteNotFoundException.class, () ->
                engine.calculateOptimalRoute("PACK", List.of("S1"), "ISOLATED_DISPATCH"));
    }

    @Test
    @DisplayName("Disconnected Shelf: Throws RouteNotFoundException when a pick node is unreachable")
    void testUnreachablePickNodeThrowsException() {
        warehouse.addLocation("S99");
        assertThrows(RouteNotFoundException.class, () ->
                engine.calculateOptimalRoute("PACK", List.of("S1", "S99"), "DISPATCH"));
    }

    @Test
    @DisplayName("Validation: Throws IllegalArgumentException for null or unknown nodes and negative weights")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateOptimalRoute("PACK", null, "DISPATCH"));
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateOptimalRoute("UNKNOWN", List.of("S1"), "DISPATCH"));
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculateOptimalRoute("PACK", List.of("UNKNOWN_SHELF"), "DISPATCH"));
        assertThrows(IllegalArgumentException.class, () ->
                warehouse.addCorridor("S1", "S2", -5));
    }

    @Test
    @DisplayName("Heuristic Strategy: GreedyNearestNeighborStrategy produces valid route")
    void testGreedyStrategy() {
        WarehouseRoutingEngine greedyEngine = new WarehouseRoutingEngine(
                warehouse, new DijkstraPathFinder(), new GreedyNearestNeighborStrategy());
        RouteResult result = greedyEngine.calculateOptimalRoute("PACK", Arrays.asList("S1", "S2", "S3"), "DISPATCH");
        assertEquals(45, result.getTotalTravelTimeSeconds());
    }
}

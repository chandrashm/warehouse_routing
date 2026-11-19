# Warehouse Routing Engine — LLD Interview Preparation Guide

## 1. Problem Summary
- **Graph Model:** Weighted directed graph where nodes represent warehouse locations (`PACK`, `S1`, `S2`, `S3`, `DISPATCH`) and edges represent one-way corridors with travel times in seconds.
- **Objective:** Start at `PACK`, visit a given subset of pick nodes in **any order**, and end at `DISPATCH` with minimum total travel time.

## 2. Clarifying Questions & Edge Cases
- **Pick List Size (K) vs. Warehouse Nodes (V):** If `K <= 15`, Held-Karp Bitmask DP gives an exact optimal answer. For `K > 15`, switch to a heuristic (`GreedyNearestNeighborStrategy`).
- **Non-Negative Weights:** Confirm travel times are `>= 0` (required for Dijkstra).
- **Edge Cases:** Empty pick list (`PACK -> DISPATCH`), duplicate items in pick list, unreachable shelf or `DISPATCH` (`RouteNotFoundException`), passing through intermediate shelves without picking.

## 3. SOLID Principles & Design Patterns
- **Single Responsibility (SRP):** `WarehouseGraph` (topology), `DijkstraPathFinder` (APSP), `ExactBitmaskRoutingStrategy` (TSP optimization), `WarehouseRoutingEngine` (validation & orchestration).
- **Strategy Pattern & Open/Closed (OCP):** `RoutingStrategy` interface allows swapping exact Bitmask DP with greedy/2-opt heuristics without touching core engine code.
- **Factory Pattern:** `RoutingStrategyFactory` selects the optimal strategy based on pick list size `K`.
- **Dependency Inversion (DIP):** `WarehouseRoutingEngine` depends on `ShortestPathFinder` and `RoutingStrategy` interfaces.

## 4. Complexity Analysis
- **Time Complexity:** `O((K + 1) * (V + E) log V + K^2 * 2^K)`
- **Space Complexity:** `O(V + E + K * 2^K)`

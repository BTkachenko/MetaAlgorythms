import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Stack;

public class TSPSolver {
    static class Edge {
        int src, dest;
        double weight;

        public Edge(int src, int dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    static ArrayList<Integer> performDFS(ArrayList<ArrayList<Integer>> adjList, int i) {
        Stack<Integer> stack = new Stack<>();
        ArrayList<Integer> visitedOrder = new ArrayList<>();

        stack.push(0);

        while (!stack.isEmpty()) {
            int current = stack.pop();
            if (!visitedOrder.contains(current)) {
                visitedOrder.add(current);
                for (int neighbor : adjList.get(current)) {
                    stack.push(neighbor);
                }
            }
        }

        // Close the cycle by returning to the start vertex
        visitedOrder.add(0);
        return visitedOrder;
    }

    public static void main(String[] args) {
        ArrayList<double[]> coordinates = new ArrayList<>();

        // Read coordinates from file
        try (InputStream in = TSPSolver.class.getResourceAsStream("/xqf131.tsp")){

            assert in != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            boolean readingCoordinates = false;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("NODE_COORD_SECTION")) {
                    readingCoordinates = true;
                    continue;
                } else if (line.trim().equals("EOF")) {
                    break;
                }

                if (readingCoordinates) {
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    coordinates.add(new double[]{x, y});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int n = coordinates.size();

        // Initialize MST set and priority queue
        boolean[] inMST = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(edge -> edge.weight));
        ArrayList<Edge> mstEdges = new ArrayList<>();

        // Start with the first vertex
        inMST[0] = true;

        // Add edges from the first vertex to the priority queue
        for (int i = 1; i < n; i++) {
            double weight = calculateDistance(coordinates.get(0)[0], coordinates.get(0)[1], coordinates.get(i)[0], coordinates.get(i)[1]);
            pq.add(new Edge(0, i, weight));
        }

        // Initialize MST weight
        double mstWeight = 0;

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();

            // Skip if the edge forms a cycle
            if (inMST[edge.dest]) continue;

            // Add edge to MST
            inMST[edge.dest] = true;
            mstWeight += edge.weight;
            mstEdges.add(edge);

            // Add all edges from this vertex to the priority queue
            for (int i = 0; i < n; i++) {
                if (!inMST[i]) {
                    double weight = calculateDistance(coordinates.get(edge.dest)[0], coordinates.get(edge.dest)[1], coordinates.get(i)[0], coordinates.get(i)[1]);
                    pq.add(new Edge(edge.dest, i, weight));
                }
            }
        }

        System.out.println("Total weight of MST: " + mstWeight);

        // Create an adjacency list from the edge list
        ArrayList<ArrayList<Integer>> adjList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<>());
        }

        for (Edge edge : mstEdges) {
            adjList.get(edge.src).add(edge.dest);
            adjList.get(edge.dest).add(edge.src);
        }


// Perform DFS to get the cycle
        ArrayList<Integer> tspCycle = performDFS(adjList, 0);

// Initialize the weight of the TSP cycle
        double tspCycleWeight = 0;

// Calculate the weight of the TSP cycle
        for (int i = 0; i < tspCycle.size() - 1; i++) {
            int src = tspCycle.get(i);
            int dest = tspCycle.get(i + 1);
            double weight = calculateDistance(coordinates.get(src)[0], coordinates.get(src)[1], coordinates.get(dest)[0], coordinates.get(dest)[1]);
            tspCycleWeight += weight;
        }

// Print the obtained cycle and its weight
        System.out.print("TSP cycle based on MST: ");
        for (int vertex : tspCycle) {
            System.out.print(vertex + " -> ");
        }
        System.out.println();
        System.out.println("Total weight of TSP cycle based on MST: " + tspCycleWeight);

    }
}

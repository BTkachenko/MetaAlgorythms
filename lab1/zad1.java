import java.io.*;
import java.util.*;

public class zad1 {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Proszę podać ścieżkę do pliku .tsp jako argument.");
            return;
        }
        String filePath = args[0];
        double[][] coordinates = readTSPFile(filePath);

        if (coordinates == null) {
            System.out.println("Nie udało się wczytać danych.");
            return;
        }

        MSTResult mstResult = calculateMST(coordinates);
        System.out.println("Waga minimalnego drzewa rozpinającego: " + mstResult.weight);

        int numIterations = (int) Math.ceil(Math.sqrt(coordinates.length));
        double totalDistance = 0;
        double bestDistance = Double.MAX_VALUE;
        int totalImprovementSteps = 0;

        Random random = new Random();
        for (int i = 0; i < numIterations; i++) {
            int randomVertex = random.nextInt(coordinates.length);
            List<Integer> cycle = constructCycleFromMST(mstResult.mst, coordinates.length, randomVertex);
            LocalSearchResult result = localSearch(cycle, coordinates);
            totalDistance += result.distance;
            totalImprovementSteps += result.steps;

            if (result.distance < bestDistance) {
                bestDistance = result.distance;
            }
        }

        double averageDistance = totalDistance / numIterations;
        double averageImprovementSteps = (double) totalImprovementSteps / numIterations;

        System.out.println("Średnia wartość rozwiązania: " + averageDistance);
        System.out.println("Średnia liczba kroków poprawy: " + averageImprovementSteps);
        System.out.println("Najlepsze znalezione rozwiązanie: " + bestDistance);
    }

    private static double[][] readTSPFile(String filePath) {
        List<double[]> coordList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("EOF")) {
                    break;
                }
                if (line.matches("\\d+\\s+\\d+\\.?\\d*\\s+\\d+\\.?\\d*")) {
                    String[] parts = line.trim().split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    coordList.add(new double[]{x, y});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return coordList.toArray(new double[0][]);
    }

    private static MSTResult calculateMST(double[][] coordinates) {
        int n = coordinates.length;
        double[] key = new double[n];
        int[] parent = new int[n];
        boolean[] mstSet = new boolean[n];
        Arrays.fill(key, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        key[0] = 0;
        double totalWeight = 0;

        for (int count = 0; count < n - 1; count++) {
            int u = minKey(key, mstSet);
            mstSet[u] = true;
            for (int v = 0; v < n; v++) {
                double weight = distance(coordinates[u], coordinates[v]);
                if (!mstSet[v] && weight < key[v]) {
                    parent[v] = u;
                    key[v] = weight;
                }
            }
        }

        List<Edge> edges = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            edges.add(new Edge(parent[i], i, key[i]));
            totalWeight += key[i];
        }

        return new MSTResult(edges, totalWeight);
    }

    private static int minKey(double[] key, boolean[] mstSet) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int v = 0; v < key.length; v++) {
            if (!mstSet[v] && key[v] < min) {
                min = key[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    private static List<Integer> constructCycleFromMST(List<Edge> mst, int numVertices, int startVertex) {
        Map<Integer, List<Integer>> adjList = new HashMap<>();
        for (Edge edge : mst) {
            adjList.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge.to);
            adjList.computeIfAbsent(edge.to, k -> new ArrayList<>()).add(edge.from);
        }

        List<Integer> cycle = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        dfs(startVertex, adjList, visited, cycle);
        cycle.add(startVertex); // Complete the cycle
        return cycle;
    }

    private static void dfs(int v, Map<Integer, List<Integer>> adjList, boolean[] visited, List<Integer> cycle) {
        visited[v] = true;
        cycle.add(v);
        if (adjList.containsKey(v)) {
            for (int u : adjList.get(v)) {
                if (!visited[u]) {
                    dfs(u, adjList, visited, cycle);
                }
            }
        }
    }

    private static LocalSearchResult localSearch(List<Integer> path, double[][] coordinates) {
        double currentDistance = calculateTotalDistance(path, coordinates);
        int steps = 0;
        Random random = new Random();

        for (int i = 0; i < path.size() - 1; i++) {
            int bestI = i;
            int bestJ = i + 1;
            double bestDistance = currentDistance;

            for (int k = 0; k < path.size(); k++) {
                int j = random.nextInt(path.size());
                if (j == i) continue;

                Collections.swap(path, i, j);
                double newDistance = calculateTotalDistance(path, coordinates);
                if (newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestI = i;
                    bestJ = j;
                }
                Collections.swap(path, i, j); // Swap back
            }

            if (bestJ != i + 1) {
                Collections.swap(path, bestI, bestJ);
                currentDistance = bestDistance;
                steps++;
            }
        }

        return new LocalSearchResult(path, currentDistance, steps);
    }

    private static double calculateTotalDistance(List<Integer> path, double[][] coordinates) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += distance(coordinates[path.get(i)], coordinates[path.get(i + 1)]);
        }
        totalDistance += distance(coordinates[path.get(path.size() - 1)], coordinates[path.get(0)]);
        return totalDistance;
    }

    private static double distance(double[] city1, double[] city2) {
        return Math.sqrt(Math.pow(city1[0] - city2[0], 2) + Math.pow(city1[1] - city2[1], 2));
    }

    private static class MSTResult {
        List<Edge> mst;
        double weight;

        public MSTResult(List<Edge> mst, double weight) {
            this.mst = mst;
            this.weight = weight;
        }
    }

    private static class LocalSearchResult {
        List<Integer> path;
        double distance;
        int steps;

        public LocalSearchResult(List<Integer> path, double distance, int steps) {
            this.path = path;
            this.distance = distance;
            this.steps = steps;
        }
    }

    private static class Edge {
        int from;
        int to;
        double weight;

        public Edge(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
}

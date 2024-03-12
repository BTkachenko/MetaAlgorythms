import java.io.*;
import java.util.*;

public class zad2 {

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

        int n = coordinates.length;
        double bestDistance = Double.MAX_VALUE;
        double totalDistance = 0;
        int totalImprovementSteps = 0;

        for (int i = 0; i < n; i++) {
            List<Integer> permutation = randomPermutation(n);
            LocalSearchResult result = localSearch(permutation, coordinates, 10); // Przykładowa liczba sąsiadów
            totalDistance += result.distance;
            totalImprovementSteps += result.steps;

            if (result.distance < bestDistance) {
                bestDistance = result.distance;
            }
        }

        System.out.println("Średnia wartość rozwiązania: " + (totalDistance / n));
        System.out.println("Średnia liczba kroków poprawy: " + ((double) totalImprovementSteps / n));
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

    private static LocalSearchResult localSearch(List<Integer> path, double[][] coordinates, int numNeighbors) {
        double currentDistance = calculateTotalDistance(path, coordinates);
        int steps = 0;
        Random random = new Random();

        for (int i = 0; i < path.size() - 1; i++) {
            boolean improved = false;
            for (int k = 0; k < numNeighbors; k++) {
                int j = random.nextInt(path.size());
                if (j == i) continue;

                Collections.swap(path, i, j);
                double newDistance = calculateTotalDistance(path, coordinates);
                if (newDistance < currentDistance) {
                    currentDistance = newDistance;
                    steps++;
                    improved = true;
                    break; // Break the inner loop as soon as improvement is found
                }
                Collections.swap(path, i, j); // Swap back
            }

            if (!improved) {
                break; // Break the outer loop if no improvement is found for the current i
            }
        }

        return new LocalSearchResult(path, currentDistance, steps);
    }

    private static List<Integer> randomPermutation(int size) {
        List<Integer> permutation = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            permutation.add(i);
        }
        Collections.shuffle(permutation);
        return permutation;
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
}

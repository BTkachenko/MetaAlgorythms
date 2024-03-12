import java.io.*;
import java.util.*;

public class temp {
    private static final Random random = new Random();

    // Calculate the euclidean distance between two cities
    private static double euclideanDistance(double[] city1, double[] city2) {
        return Math.sqrt(Math.pow(city1[0] - city2[0], 2) + Math.pow(city1[1] - city2[1], 2));
    }

    // Calculate the total distance of the tour
    private static double totalDistance(int[] tour, double[][] coordinates) {
        double distance = 0;
        for (int i = 1; i < tour.length; i++) {
            distance += euclideanDistance(coordinates[tour[i - 1]], coordinates[tour[i]]);
        }
        // Add distance from the last city back to the first city
        distance += euclideanDistance(coordinates[tour[tour.length - 1]], coordinates[tour[0]]);
        return distance;
    }

    // Generate a neighboring solution
    private static int[] getNeighborInvert(int[] tour) {
        int a = random.nextInt(tour.length);
        int b = random.nextInt(tour.length);
        while (b == a) {
            b = random.nextInt(tour.length);
        }
        int start = Math.min(a, b);
        int end = Math.max(a, b);

        int[] newTour = tour.clone();
        while (start < end) {
            int temp = newTour[start];
            newTour[start] = newTour[end];
            newTour[end] = temp;
            start++;
            end--;
        }
        return newTour;
    }

    public static int[] simulatedAnnealing(double[][] coordinates, double initialTemperature, double coolingRate, int epochLength, int maxIterationsWithoutImprovement) {
        int[] currentSolution = new int[coordinates.length];
        for (int i = 0; i < currentSolution.length; i++) {
            currentSolution[i] = i;
        }
        shuffleArray(currentSolution);

        double currentCost = totalDistance(currentSolution, coordinates);
        double temperature = initialTemperature;
        int[] bestSolution = currentSolution.clone();
        double bestCost = currentCost;
        int iterationsWithoutImprovement = 0;

        while (iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
            for (int i = 0; i < epochLength; i++) {
                int[] neighbor = getNeighborInvert(currentSolution);
                double neighborCost = totalDistance(neighbor, coordinates);

                double costDifference = currentCost - neighborCost;
                double acceptanceProbability = Math.exp(costDifference / temperature);

                if (costDifference > 0 || random.nextDouble() < acceptanceProbability) {
                    currentSolution = neighbor;
                    currentCost = neighborCost;

                    if (neighborCost < bestCost) {
                        bestSolution = neighbor.clone();
                        bestCost = neighborCost;
                        iterationsWithoutImprovement = 0;
                    }
                }
            }
            iterationsWithoutImprovement++;
            temperature *= coolingRate;
        }

        return bestSolution;
    }

    // Helper method to shuffle an array
    private static void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    // Method to read TSP data from a file
    public static double[][] readTSPFile(String filename) throws IOException {
        List<double[]> coordinatesList = new ArrayList<>();
        boolean readingCoordinates = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("NODE_COORD_SECTION")) {
                    readingCoordinates = true;
                    continue;
                }
                if (line.startsWith("EOF")) {
                    break;
                }
                if (readingCoordinates) {
                    String[] parts = line.trim().split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    coordinatesList.add(new double[]{x, y});
                }
            }
        }

        return coordinatesList.toArray(new double[0][]);
    }

    // Metoda do obliczania maksymalnej odległości między dwoma punktami
    private static double calculateMaxDistance(double[][] coordinates) {
        double maxDistance = 0;
        for (int i = 0; i < coordinates.length; i++) {
            for (int j = i + 1; j < coordinates.length; j++) {
                double distance = euclideanDistance(coordinates[i], coordinates[j]);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;
    }



// Metoda testująca algorytm
public static void testAlgorithm(double[][] coordinates) {
    double[] alphas = { 0.4,0.5,0.6}; // Różne wartości dla α
    double[] betas = {0.8,0.85,0.9, 0.95}; // Różne wartości dla β
    double[] gammas = {0.2,0.4,0.5,0.6}; // Różne wartości dla γ jako procent liczby miast
    double[] deltas = {0.2,0.3,0.4,0.5}; // Różne wartości dla δ jako procent liczby miast

    double bestAverageDistance = Double.MAX_VALUE;

    double bestMinDistance = Double.MAX_VALUE;
    String bestParams = "";
    
    for (double alpha : alphas) {
        for (double beta : betas) {
            for (double gamma : gammas) {
                for (double delta : deltas) {
                    double initialTemperature = alpha * calculateMaxDistance(coordinates);
                    int epochLength = (int) (gamma * coordinates.length);
                    int maxIterationsWithoutImprovement = (int) (delta * coordinates.length);

                    double totalDistance = 0;
                    double minDistance = Double.MAX_VALUE;
                    int numberOfRuns = 10;

                    for (int run = 0; run < numberOfRuns; run++) {
                        int[] tour = SimulatedAnnealingTSP.simulatedAnnealing(coordinates, initialTemperature, beta, epochLength, maxIterationsWithoutImprovement);
                        double distance = temp.totalDistance(tour, coordinates);
                        minDistance = Math.min(minDistance, distance);
                        totalDistance += distance;
                    }

                    double averageDistance = totalDistance / numberOfRuns;
                    if (averageDistance < bestAverageDistance || (averageDistance == bestAverageDistance && minDistance < bestMinDistance)) {
                        bestAverageDistance = averageDistance;
                        bestMinDistance = minDistance;
                        bestParams = "α = " + alpha + ", β = " + beta + ", γ = " + gamma + ", δ = " + delta;
                    }
                }
            }
        }
    }

    System.out.println("Best parameters: " + bestParams);
    System.out.println("Best minimum distance: " + bestMinDistance);
    System.out.println("Best average distance: " + bestAverageDistance);
}

    public static void main(String[] args) {
        try {
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

            double maxDistance = 0;
            for (int i = 0; i < coordinates.length; i++) {
                for (int j = i + 1; j < coordinates.length; j++) {
                    double distance = euclideanDistance(coordinates[i], coordinates[j]);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                    }
                }
            }

            testAlgorithm(coordinates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
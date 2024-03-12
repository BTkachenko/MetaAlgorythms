import java.io.*;
import java.util.*;

public class tabooParams {
    private static final Random random = new Random();

// Metoda do obliczania odległości euklidesowej
private static double euclideanDistance(double[] city1, double[] city2) {
    return Math.sqrt(Math.pow(city1[0] - city2[0], 2) + Math.pow(city1[1] - city2[1], 2));
}

// Metoda do obliczania całkowitej długości trasy
private static double totalDistance(int[] tour, double[][] coordinates) {
    double distance = 0;
    for (int i = 1; i < tour.length; i++) {
        distance += euclideanDistance(coordinates[tour[i - 1]], coordinates[tour[i]]);
    }
    distance += euclideanDistance(coordinates[tour[tour.length - 1]], coordinates[tour[0]]);
    return distance;
}


private static int[] invertSegment(int[] tour, int start, int end) {
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

// Główna metoda algorytmu Tabu Search
public static int[] tabuSearch(double[][] coordinates, int tabuListSize, int maxIterationsWithoutImprovement) {
    int numCities = coordinates.length;
    int[] currentSolution = new int[numCities];
    for (int i = 0; i < numCities; i++) {
        currentSolution[i] = i;
    }
    shuffleArray(currentSolution);

    double currentDistance = totalDistance(currentSolution, coordinates);
    int[] bestSolution = currentSolution.clone();
    double bestDistance = currentDistance;

    List<int[]> tabuList = new ArrayList<>();
    int iterationsWithoutImprovement = 0;

    while (iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
        int[] bestNeighbor = null;
        double bestNeighborDistance = Double.MAX_VALUE;

        // Przeszukiwanie pełnego otoczenia
        for (int i = 0; i < numCities - 1; i++) {
            for (int j = i + 1; j < numCities; j++) {
                int[] neighbor = invertSegment(currentSolution, i, j);
                double neighborDistance = totalDistance(neighbor, coordinates);

                if (!tabuList.contains(neighbor) && neighborDistance < bestNeighborDistance) {
                    bestNeighbor = neighbor;
                    bestNeighborDistance = neighborDistance;
                }
            }
        }

        if (bestNeighbor != null) {
            currentSolution = bestNeighbor;
            currentDistance = bestNeighborDistance;

            if (currentDistance < bestDistance) {
                bestSolution = currentSolution.clone();
                bestDistance = currentDistance;
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
            }

            // Aktualizacja listy tabu
            tabuList.add(bestNeighbor);
            if (tabuList.size() > tabuListSize) {
                tabuList.remove(0);
            }
        }
    }

    return bestSolution;
}

// Pomocnicza metoda do mieszania tablicy
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

// Metoda do testowania różnych parametrów
public static void optimizeParameters(double[][] coordinates) {
    double[] alphas = {0.05,0.1, 0.3}; // Różne wartości dla α
    double[] betas = {0.1, 0.2, 0.3,0.5};    // Różne wartości dla β

    double bestOverallDistance = Double.MAX_VALUE;
    double bestAlpha = 0;
    double bestBeta = 0;

    for (double alpha : alphas) {
        for (double beta : betas) {
            int tabuListSize = (int) (alpha * coordinates.length);
            int maxIterationsWithoutImprovement = (int) (beta * coordinates.length);

            double totalDistance = 0;
            double bestDistance = Double.MAX_VALUE;
            int numberOfRuns = 20;

            for (int run = 0; run < numberOfRuns; run++) {
                int[] bestTour = tabuSearch(coordinates, tabuListSize, maxIterationsWithoutImprovement);
                double distance = totalDistance(bestTour, coordinates);

                if (distance < bestDistance) {
                    bestDistance = distance;
                }
                totalDistance += distance;
            }

            double averageDistance = totalDistance / numberOfRuns;
            if (bestDistance < bestOverallDistance) {
                bestOverallDistance = bestDistance;
                bestAlpha = alpha;
                bestBeta = beta;
            }

            System.out.println("For alpha = " + alpha + ", beta = " + beta + ":");
            System.out.println("Best distance: " + bestDistance);
            System.out.println("Average distance: " + averageDistance);
        }
    }

    System.out.println("\nBest overall parameters found: ");
    System.out.println("Alpha (Tabu List Size): " + bestAlpha);
    System.out.println("Beta (Iterations Without Improvement): " + bestBeta);
    System.out.println("Best overall tour distance: " + bestOverallDistance);
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
             optimizeParameters(coordinates);
        }catch (IOException e) {
                e.printStackTrace();
            }
            
       
    }
}
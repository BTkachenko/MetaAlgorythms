import java.io.*;
import java.util.*;


public class TabooSearchTSP {
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

        Set<Pair> tabuList = new HashSet<>();
        int iterationsWithoutImprovement = 0;

        while (iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
            Pair bestMove = null;
            double bestMoveDistance = Double.MAX_VALUE;

            for (int i = 0; i < numCities - 1; i++) {
                int j = random.nextInt(numCities);
                Pair move = new Pair(i, j);
                if (i != j && !tabuList.contains(move)) {
                    int[] neighbor = invertSegment(currentSolution, i, j);
                    double neighborDistance = totalDistance(neighbor, coordinates);

                    if (neighborDistance < bestMoveDistance) {
                        bestMoveDistance = neighborDistance;
                        bestMove = move;
                    }
                }
            }

            if (bestMove != null) {
                applyMove(currentSolution, bestMove);
                currentDistance = bestMoveDistance;

                if (currentDistance < bestDistance) {
                    bestSolution = currentSolution.clone();
                    bestDistance = currentDistance;
                    iterationsWithoutImprovement = 0;
                } else {
                    iterationsWithoutImprovement++;
                }

                tabuList.add(bestMove);
                if (tabuList.size() > tabuListSize) {
                    tabuList.remove(tabuList.iterator().next());
                }
            }
        }

        return bestSolution;
    }

    private static void applyMove(int[] solution, Pair move) {
        int start = move.getFirst();
        int end = move.getSecond();
        while (start < end) {
            int temp = solution[start];
            solution[start] = solution[end];
            solution[end] = temp;
            start++;
            end--;
        }
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
    // Klasa do przechowywania par indeksów
    static class Pair {
        private final int first;
        private final int second;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return first == pair.first && second == pair.second;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
    // Metoda main
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



            
            double bestTourDistanceEver = Double.MAX_VALUE;
            double totalDistance = 0;
            int numberOfRuns = 3;

            // Parametry algorytmu
        int tabuListSize = 7;
        int maxIterationsWithoutImprovement = 15;




        for (int run = 0; run < numberOfRuns; run++) {

         int[] bestTour = tabuSearch(coordinates, tabuListSize, maxIterationsWithoutImprovement);
         double bestTourDistance = totalDistance(bestTour, coordinates);
                if (bestTourDistance < bestTourDistanceEver) {
                    bestTourDistanceEver = bestTourDistance;
                }
                totalDistance += bestTourDistance;
            }

            double averageDistance = totalDistance / numberOfRuns;

            System.out.println("Best tour distance ever: " + bestTourDistanceEver);
            System.out.println("Average tour distance: " + averageDistance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

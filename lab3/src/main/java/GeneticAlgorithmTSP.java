import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class GeneticAlgorithmTSP {
    private static final Random random = new Random();

    // Metoda obliczająca odległość euklidesową między miastami
    private static double euclideanDistance(double[] city1, double[] city2) {
        return Math.sqrt(Math.pow(city1[0] - city2[0], 2) + Math.pow(city1[1] - city2[1], 2));
    }

    // Oblicz całkowitą odległość podróży
    private static double totalDistance(int[] tour, double[][] coordinates) {
        double distance = 0;
        for (int i = 1; i < tour.length; i++) {
            distance += euclideanDistance(coordinates[tour[i - 1]], coordinates[tour[i]]);
        }
        distance += euclideanDistance(coordinates[tour[tour.length - 1]], coordinates[tour[0]]);
        return distance;
    }


    // Metoda do losowego mieszania tablicy
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
    //Selekcja Turnejowa
    private static int[] selectParent(int[][] population, double[][] coordinates) {
        int tournamentSize = 5;
        int best = random.nextInt(population.length);
        double bestFitness = totalDistance(population[best], coordinates);

        for (int i = 1; i < tournamentSize; i++) {
            int index = random.nextInt(population.length);
            double fitness = totalDistance(population[index], coordinates);
            if (fitness < bestFitness) {
                best = index;
                bestFitness = fitness;
            }
        }
        return population[best];
    }

    // Implementacja metod krzyżowania
    private static int[] pmxCrossover(int[] parent1, int[] parent2) {
        int length = parent1.length;
        int[] child = new int[length];
        Arrays.fill(child, -1); // Inicjalizacja dziecka z wartościami -1

        // Losowe wybieranie dwóch punktów krzyżowania
        int start = random.nextInt(length);
        int end = random.nextInt(length - start) + start;

        // Kopiowanie segmentu z pierwszego rodzica do dziecka
        for (int i = start; i <= end; i++) {
            child[i] = parent1[i];
        }

        // Wypełnianie pozostałych miejsc w dziecku elementami z drugiego rodzica
        for (int i = 0; i < start; i++) {
            if (!contains(child, parent2[i])) {
                child[i] = parent2[i];
            }
        }
        for (int i = end + 1; i < length; i++) {
            if (!contains(child, parent2[i])) {
                child[i] = parent2[i];
            }
        }

        // Naprawianie brakujących elementów
        for (int i = 0; i < length; i++) {
            if (child[i] == -1) {
                for (int j = 0; j < length; j++) {
                    if (!contains(child, parent2[j])) {
                        child[i] = parent2[j];
                        break;
                    }
                }
            }
        }

        return child;
    }
    private static boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    private static int[] oxCrossover(int[] parent1, int[] parent2) {
        int length = parent1.length;
        int[] child = new int[length];
        Arrays.fill(child, -1);

        int start = random.nextInt(length);
        int end = random.nextInt(length);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        for (int i = start; i <= end; i++) {
            child[i] = parent1[i];
        }

        int currentIndex = 0;
        for (int i = 0; i < length; i++) {
            if (!contains(child, parent2[i])) {
                while (currentIndex >= start && currentIndex <= end) {
                    currentIndex++;
                }
                child[currentIndex++] = parent2[i];
            }
        }
        return child;
    }

    // Mutacja
    private static void mutateSwap(int[] tour) {
        int index1 = random.nextInt(tour.length);
        int index2 = random.nextInt(tour.length);
        while (index1 == index2) {
            index2 = random.nextInt(tour.length);
        }
        int temp = tour[index1];
        tour[index1] = tour[index2];
        tour[index2] = temp;
    }



    // Główny algorytm GA
    private static int[] geneticAlgorithm(double[][] coordinates, int populationSize, int generations, double mutationRate) {
        int[][] population = new int[populationSize][];
        for (int i = 0; i < populationSize; i++) {
            population[i] = new int[coordinates.length];
            for (int j = 0; j < coordinates.length; j++) {
                population[i][j] = j;
            }
            shuffleArray(population[i]);
        }

        double bestDistance = Double.MAX_VALUE;
        int[] bestTour = null;

        for (int gen = 0; gen < generations; gen++) {
            int[][] newPopulation = new int[populationSize][];

            for (int i = 0; i < populationSize; i++) {
                int[] parent1 = selectParent(population, coordinates);
                int[] parent2 = selectParent(population, coordinates);

                int[] child;
                // Alternatywne stosowanie PMX i OX dla różnorodności
                if (random.nextDouble() < 0.5) {
                    child = pmxCrossover(parent1, parent2);
                } else {
                    child = oxCrossover(parent1, parent2);
                }

                if (random.nextDouble() < mutationRate) {
                    mutateSwap(child);
                }
                newPopulation[i] = child;

                double childDistance = totalDistance(child, coordinates);
                if (childDistance < bestDistance) {
                    bestDistance = childDistance;
                    bestTour = child.clone();
                }
            }
            population = newPopulation;
        }

        return bestTour;
    }


    // Metoda uruchamiająca algorytm wyspowy
    private static int[] runIslandModel(double[][] coordinates, int populationSize, int epochs, int iterationsPerEpoch, double crossoverProbability, double mutationRate, int numberOfIslands) {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfIslands);
        int[][] bestIndividuals = new int[numberOfIslands][];
        double[] bestDistances = new double[numberOfIslands];
        Arrays.fill(bestDistances, Double.MAX_VALUE);
        int[] bestGlobalTour = null;
        double bestGlobalDistance = Double.MAX_VALUE;

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch: " + (epoch + 1) + "/" + epochs);
            for (int island = 0; island < numberOfIslands; island++) {
                final int islandId = island;
                executor.submit(() -> {
                    int[] bestTour = geneticAlgorithm(coordinates, populationSize / numberOfIslands, iterationsPerEpoch, mutationRate);
                    double distance = totalDistance(bestTour, coordinates);
                    synchronized (bestIndividuals) {
                        if (distance < bestDistances[islandId]) {
                            bestDistances[islandId] = distance;
                            bestIndividuals[islandId] = bestTour;
                        }
                    }
                });
            }

            try {
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            executor = Executors.newFixedThreadPool(numberOfIslands); // Restart executora dla nowej epoki

            // Wymiana najlepszych osobników między wyspami
            if (epoch < epochs - 1) { // Pomiń wymianę w ostatniej epoce
                exchangeBestIndividuals(bestIndividuals, bestDistances);
            }
        }

        // Wyszukaj globalnie najlepszy wynik
        for (int i = 0; i < numberOfIslands; i++) {
            if (bestDistances[i] < bestGlobalDistance) {
                bestGlobalDistance = bestDistances[i];
                bestGlobalTour = bestIndividuals[i];
            }
        }

        executor.shutdownNow();

        return bestGlobalTour;
    }

    private static void exchangeBestIndividuals(int[][] bestIndividuals, double[] bestDistances) {
        // Prosta wymiana: najlepszy z każdej wyspy z następnym
        for (int i = 0; i < bestIndividuals.length; i++) {
            int next = (i + 1) % bestIndividuals.length;
            if (bestDistances[next] > bestDistances[i]) {
                bestIndividuals[next] = bestIndividuals[i].clone();
                bestDistances[next] = bestDistances[i];
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Proszę podać ścieżkę do pliku TSP jako argument.");
            return;
        }
        String filePath = args[0]; // Ścieżka do pliku TSP z argumentów

        double[][] coordinates; // Wczytanie współrzędnych miast z pliku TSP
        try {
            coordinates = readTSPFile(filePath);
        } catch (IOException e) {
            System.err.println("Wystąpił błąd przy wczytywaniu pliku: " + e.getMessage());
            return;
        }

        if (coordinates.length == 0) {
            System.out.println("Plik nie zawiera danych lub są one nieprawidłowe.");
            return;
        }

        // Parametry algorytmu wyspowego genetycznego
        int populationSize = 1000; // Rozmiar populacji
        int numberOfIslands = 8; // Liczba wysp
        int epochs = 10; // Liczba epok
        int iterationsPerEpoch = 1000; // Liczba iteracji w epoce
        double crossoverProbability = 0.85; // Prawdopodobieństwo krzyżowania
        double mutationRate = 0.5; // Prawdopodobieństwo mutacji

        System.out.println("Rozpoczęcie algorytmu genetycznego...");
        long startTime = System.currentTimeMillis();

        int[] bestTour = runIslandModel(coordinates, populationSize, epochs, iterationsPerEpoch, crossoverProbability, mutationRate, numberOfIslands);

        // Wyświetlenie wyników
        System.out.println("Zakończono algorytm genetyczny.");
        if (bestTour != null) {
           // System.out.println("Najlepsza znaleziona trasa: " + Arrays.toString(bestTour));
            System.out.println("Długość najlepszej trasy: " + totalDistance(bestTour, coordinates));
        } else {
            System.out.println("Nie znaleziono rozwiązania.");
        }
        long endTime = System.currentTimeMillis();

        // Wyświetlenie czasu wykonania
        System.out.println("Czas wykonania: " + (endTime - startTime) + "ms");
    }

}

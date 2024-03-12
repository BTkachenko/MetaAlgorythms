import java.io.*;
import java.util.*;

import java.io.*;
import java.util.*;

public class Zadanie3 {

    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static void main(String[] args) throws IOException {
        File folder = new File(Zadanie3.class.getResource("/").getPath());
        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".tsp"));

        if (listOfFiles == null) {
            System.out.println("No TSP files found!");
            return;
        }

        for (File file : listOfFiles) {
            String fileName = file.getName();
            ArrayList<double[]> coordinates = new ArrayList<>();

            InputStream in = Zadanie3.class.getResourceAsStream("/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            boolean readCoordinates = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("EOF")) {
                    break;
                }
                if (readCoordinates) {
                    String[] parts = line.split(" ");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    coordinates.add(new double[]{x, y});
                }
                if (line.startsWith("NODE_COORD_SECTION")) {
                    readCoordinates = true;
                }
            }
            reader.close();

            Random rand = new Random();
            List<Double> minListTen = new ArrayList<>();
            List<Double> minListFifty = new ArrayList<>();
            double overallMin = Double.MAX_VALUE;

            for (int i = 0; i < 100; i++) {
                double localMinTen = Double.MAX_VALUE;
                for (int j = 0; j < 10; j++) {
                    Collections.shuffle(coordinates, rand);
                    double totalDistance = 0;
                    for (int k = 0; k < coordinates.size() - 1; k++) {
                        double[] point1 = coordinates.get(k);
                        double[] point2 = coordinates.get(k + 1);
                        totalDistance += calculateDistance(point1[0], point1[1], point2[0], point2[1]);
                    }
                    double[] firstPoint = coordinates.get(0);
                    double[] lastPoint = coordinates.get(coordinates.size() - 1);
                    totalDistance += calculateDistance(firstPoint[0], firstPoint[1], lastPoint[0], lastPoint[1]);
                    localMinTen = Math.min(localMinTen, totalDistance);
                    overallMin = Math.min(overallMin, totalDistance);
                }
                minListTen.add(localMinTen);
            }

            for (int i = 0; i < 20; i++) {
                double localMinFifty = Double.MAX_VALUE;
                for (int j = 0; j < 50; j++) {
                    Collections.shuffle(coordinates, rand);
                    double totalDistance = 0;
                    for (int k = 0; k < coordinates.size() - 1; k++) {
                        double[] point1 = coordinates.get(k);
                        double[] point2 = coordinates.get(k + 1);
                        totalDistance += calculateDistance(point1[0], point1[1], point2[0], point2[1]);
                    }
                    double[] firstPoint = coordinates.get(0);
                    double[] lastPoint = coordinates.get(coordinates.size() - 1);
                    totalDistance += calculateDistance(firstPoint[0], firstPoint[1], lastPoint[0], lastPoint[1]);
                    localMinFifty = Math.min(localMinFifty, totalDistance);
                    overallMin = Math.min(overallMin, totalDistance);
                }
                minListFifty.add(localMinFifty);
            }

            double avgOfTen = minListTen.stream().mapToDouble(val -> val).average().orElse(0.0);
            double avgOfFifty = minListFifty.stream().mapToDouble(val -> val).average().orElse(0.0);

            String outputFileName = "result" + fileName.split("\\.")[0] + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(outputFileName));
            writer.println("Average of 10-minimum groups: " + avgOfTen);
            writer.println("Average of 50-minimum groups: " + avgOfFifty);
            writer.println("Overall minimum: " + overallMin);
            writer.close();
        }
    }
}


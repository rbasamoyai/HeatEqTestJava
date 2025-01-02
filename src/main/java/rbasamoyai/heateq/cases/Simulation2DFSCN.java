package rbasamoyai.heateq.cases;

import com.opencsv.CSVWriter;
import rbasamoyai.heateq.RodSimulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Simulation2DFSCN {

    public static void run() {
        double dt = 1d / 20d;
        int steps = 1000;

        int xDim = 20;
        int yDim = 20;

        long startTimeNanos = System.nanoTime();
        // initialize 2d cells with random values
        Random random = new Random();
        double valueScale = 100;
        double[][] cellValues = new double[xDim][yDim];

        for (int xi = 0; xi < xDim; ++xi) {
            double[] yAxis = new double[yDim];
            for (int yi = 0; yi < yDim; ++yi)
                yAxis[yi] = random.nextDouble() * valueScale;
            cellValues[xi] = yAxis;
        }
        double[][] cellOriginal = new double[xDim][yDim];
        for (int xi = 0; xi < xDim; ++xi) {
            double[] yAxis = new double[yDim];
            System.arraycopy(cellValues[xi], 0, yAxis, 0, yDim);
            cellOriginal[xi] = yAxis;
        }

        double diffScale = 10;
        double[][] yCoeffRods = new double[xDim][yDim];
        double[][] xCoeffRods = new double[yDim][xDim];

        // set up y-direction coefficient rods (indexed X, sized Y)
        for (int xi = 0; xi < xDim; ++xi) {
            double[] yRod = new double[yDim];
            for (int yi = 0; yi < yDim; ++yi)
                yRod[yi] = random.nextDouble() * diffScale * dt * 0.5;
            yCoeffRods[xi] = yRod;
        }
        // set up x-direction coefficient rods (indexed Y, sized X) by copying from y-direction coefficient rods
        for (int yi = 0; yi < yDim; ++yi) {
            double[] xRod = new double[xDim];
            for (int xi = 0; xi < xDim; ++xi)
                xRod[xi] = yCoeffRods[xi][yi];
            xCoeffRods[yi] = xRod;
        }

        // set up x-direction value rods (indexed Y, sized X)
        double[][] xValueRods = new double[yDim][xDim];
        for (int yi = 0; yi < yDim; ++yi) {
            double[] xRod = new double[xDim];
            for (int xi = 0; xi < xDim; ++xi)
                xRod[xi] = cellValues[xi][yi];
            xValueRods[yi] = xRod;
        }
        // set up y-direction rods, but don't fill (indexed X, sized Y)
        double[][] yValueRods = new double[xDim][yDim];
        for (int xi = 0; xi < xDim; ++xi)
            yValueRods[xi] = new double[yDim];

        double[] xRodBuf1 = new double[xDim];
        double[] xRodBuf2 = new double[xDim];
        double[] yRodBuf1 = new double[yDim];
        double[] yRodBuf2 = new double[yDim];
        long endTimeNanos = System.nanoTime();
        long initTimeNanos = endTimeNanos - startTimeNanos;

        double initTime = initTimeNanos * 1e-9d;

        System.out.println();
        System.out.printf("Initialization time   : %16.10f s%n", initTime);

        // simulate 2d block
        startTimeNanos = System.nanoTime();
        for (int stepI = 0; stepI < steps; ++stepI) {
            // Start with x direction
            for (int yi = 0; yi < yDim; ++yi) {
                double[] xRodT = xValueRods[yi];
                double[] xRotK = xCoeffRods[yi];
                RodSimulation.stepRodSimulation(xRodT, xRodBuf1, xRodBuf2, xRotK, dt);
            }
            // Copy x to y rods
            for (int xi = 0; xi < xDim; ++xi) {
                double[] yRod = yValueRods[xi];
                for (int yi = 0; yi < yDim; ++yi)
                    yRod[yi] = xValueRods[yi][xi];
            }
            // Then y direction
            for (int xi = 0; xi < yDim; ++xi) {
                double[] yRodT = yValueRods[xi];
                double[] yRotK = yCoeffRods[xi];
                RodSimulation.stepRodSimulation(yRodT, yRodBuf1, yRodBuf2, yRotK, dt);
            }
            // Copy y rods to x rods
            for (int yi = 0; yi < yDim; ++yi) {
                double[] xRod = xValueRods[yi];
                for (int xi = 0; xi < xDim; ++xi)
                    xRod[xi] = yValueRods[xi][yi];
            }
        }
        endTimeNanos = System.nanoTime();
        long simTimeNanos = endTimeNanos - startTimeNanos;
        double simTime = simTimeNanos * 1e-9d;

        System.out.println();
        System.out.printf("Simulation time       : %16.10f s%n", simTime);
        System.out.printf("Simulated time        : %16.10f s%n", dt * steps);
        System.out.printf("Average time per tick : %16.10f s%n", simTime / steps);
        System.out.printf("Time per tick to beat : %16.10f s%n", dt);

        // copy back xValueRods to cellValues
        for (int yi = 0; yi < yDim; ++yi) {
            double[] xRod = xValueRods[yi];
            for (int xi = 0; xi < xDim; ++xi)
                cellValues[xi][yi] = xRod[xi];
        }

        // Write to output
        Path output = Path.of("run", "output2d.csv");
        if (!Files.exists(output.getParent())) {
            try {
                Files.createDirectory(output.getParent());
            } catch (IOException e) {
                return;
            }
        }
        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(output))) {
            for (int yi = 0; yi < yDim; ++yi) {
                String[] xRodStr = new String[xDim];
                for (int xi = 0; xi < xDim; ++xi)
                    xRodStr[xi] = Double.toString(cellOriginal[xi][yi]);
                writer.writeNext(xRodStr, false);
            }
            writer.writeNext(new String[]{}, false);
            for (int yi = 0; yi < yDim; ++yi) {
                String[] xRodStr = new String[xDim];
                for (int xi = 0; xi < xDim; ++xi)
                    xRodStr[xi] = Double.toString(cellValues[xi][yi]);
                writer.writeNext(xRodStr, false);
            }
            writer.writeNext(new String[]{}, false);
            for (int yi = 0; yi < yDim; ++yi) {
                String[] xRodStr = new String[xDim];
                double[] xCoeffRod = xCoeffRods[yi];
                for (int xi = 0; xi < xDim; ++xi)
                    xRodStr[xi] = Double.toString(xCoeffRod[xi]);
                writer.writeNext(xRodStr, false);
            }
        } catch (Exception e) {

        }
    }

    private Simulation2DFSCN() {}

}

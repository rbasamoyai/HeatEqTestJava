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
            double[] yAxis = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yAxis[yi] = random.nextDouble() * valueScale;
        }
        double[][] cellOriginal = new double[xDim][yDim];
        for (int xi = 0; xi < xDim; ++xi) {
            double[] yAxis = cellOriginal[xi];
            double[] yAxisSrc = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yAxis[yi] = yAxisSrc[yi];
        }

        double diffScale = 80;
        double[][] yCoeffRods = new double[xDim][yDim];
        double[][] xCoeffRods = new double[yDim][xDim];

        // set up y-direction coefficient rods (indexed X, sized Y)
        for (int xi = 0; xi < xDim; ++xi) {
            double[] yRod = yCoeffRods[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yRod[yi] = random.nextDouble() * diffScale * dt * 0.5;
        }
        // set up x-direction coefficient rods (indexed Y, sized X) by copying from y-direction coefficient rods
        for (int yi = 0; yi < yDim; ++yi) {
            double[] xRod = xCoeffRods[yi];
            for (int xi = 0; xi < xDim; ++xi)
                xRod[xi] = yCoeffRods[xi][yi];
        }

        // set up x-direction value rods (indexed Y, sized X)
        double[][] xValueRods = new double[yDim][xDim];
        for (int yi = 0; yi < yDim; ++yi) {
            double[] xRod = xValueRods[yi];
            for (int xi = 0; xi < xDim; ++xi)
                xRod[xi] = cellValues[xi][yi];
        }
        // set up y-direction rods, but don't fill (indexed X, sized Y)
        double[][] yValueRods = new double[xDim][yDim];

        int maxDim = Math.max(xDim, yDim);
        double[] rodBuf1 = new double[maxDim];
        double[] rodBuf2 = new double[maxDim];
        long endTimeNanos = System.nanoTime();
        long initTimeNanos = endTimeNanos - startTimeNanos;

        double initTimeMillis = initTimeNanos * 1e-6d;

        System.out.println();
        System.out.printf("Initialization time   : %12.8f ms%n", initTimeMillis);

        // simulate 2d block
        startTimeNanos = System.nanoTime();
        for (int stepI = 0; stepI < steps; ++stepI) {
            // Start with x direction
            for (int yi = 0; yi < yDim; ++yi) {
                double[] xRodT = xValueRods[yi];
                double[] xRodK = xCoeffRods[yi];
                RodSimulation.stepRodSimulation(xDim, xRodT, rodBuf1, rodBuf2, xRodK, dt, 0, 0);
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
                RodSimulation.stepRodSimulation(yDim, yRodT, rodBuf1, rodBuf2, yRotK, dt, 0, 0);
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
        double simTimeMillis = simTimeNanos * 1e-6d;

        System.out.println();
        System.out.printf("Simulation time       : %12.8f ms%n", simTimeMillis);
        System.out.printf("Simulated time        : %12.8f s%n", dt * steps);
        System.out.printf("Average time per tick : %12.8f ms%n", simTimeMillis / steps);
        System.out.printf("Time per tick to beat : %12.8f ms%n", dt * 1000);
        System.out.printf("Percent of tick time  : %7.3f%%%n", simTimeMillis / 1000 / dt);

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

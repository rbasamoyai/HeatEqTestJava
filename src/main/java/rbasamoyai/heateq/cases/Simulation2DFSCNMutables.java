package rbasamoyai.heateq.cases;

import com.opencsv.CSVWriter;
import rbasamoyai.heateq.RodSimulation;
import rbasamoyai.heateq.data.MutableDouble;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class Simulation2DFSCNMutables {

    public static void run() {
        // Using mutables instead of copying primitives.
        double dt = 1d / 20d;
        int steps = 1000;

        int xDim = 20;
        int yDim = 20;

        long startTimeNanos = System.nanoTime();
        // initialize 2d cells with random values
        Random random = new Random();
        double valueScale = 100;
        MutableDouble[][] cellValues = new MutableDouble[xDim][yDim];

        for (int xi = 0; xi < xDim; ++xi) {
            MutableDouble[] yAxis = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yAxis[yi] = new MutableDouble(random.nextDouble() * valueScale);
        }
        // Not changed, so immutable
        double[][] cellOriginal = new double[xDim][yDim];
        for (int xi = 0; xi < xDim; ++xi) {
            double[] yAxis = cellOriginal[xi];
            MutableDouble[] mutYAxis = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yAxis[yi] = mutYAxis[yi].getValue();
        }

        double diffScale = 10;
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
        MutableDouble[][] xValueRods = new MutableDouble[yDim][xDim];
        for (int yi = 0; yi < yDim; ++yi) {
            MutableDouble[] xRod = xValueRods[yi];
            for (int xi = 0; xi < xDim; ++xi)
                xRod[xi] = cellValues[xi][yi];
        }
        // set up y-direction rods (indexed X, sized Y)
        // This differs from the primitive version that does not fill these values.
        // This is a bit inefficient as cellValues is similarly indexed, but whatever
        MutableDouble[][] yValueRods = new MutableDouble[xDim][yDim];
        for (int xi = 0; xi < xDim; ++xi) {
            MutableDouble[] yRod = yValueRods[xi];
            for (int yi = 0; yi < yDim; ++yi)
                yRod[yi] = cellValues[xi][yi];
        }

        int maxDim = Math.max(xDim, yDim);
        double[] rodTBuf = new double[maxDim]; // Initialize only once
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
                MutableDouble[] xRodT = xValueRods[yi];
                int sz = xRodT.length;

                // Copy to buffer
                for (int di = 0; di < sz; ++di)
                    rodTBuf[di] = xRodT[di].getValue();

                RodSimulation.stepRodSimulation(sz, rodTBuf, rodBuf1, rodBuf2, xCoeffRods[yi], dt, 0, 0);

                // Write back
                for (int di = 0; di < sz; ++di)
                    xRodT[di].setValue(rodTBuf[di]);
            }
            // Then y direction
            for (int xi = 0; xi < xDim; ++xi) {
                MutableDouble[] yRodT = yValueRods[xi];
                int sz = yRodT.length;

                for (int di = 0; di < sz; ++di)
                    rodTBuf[di] = yRodT[di].getValue();

                RodSimulation.stepRodSimulation(sz, rodTBuf, rodBuf1, rodBuf2, yCoeffRods[xi], dt, 0, 0);

                for (int di = 0; di < sz; ++di)
                    yRodT[di].setValue(rodTBuf[di]);
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

        // Write to output
        Path output = Path.of("run", "output2d_2.csv");
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
                    xRodStr[xi] = Double.toString(cellValues[xi][yi].getValue());
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

    private Simulation2DFSCNMutables() {}

}

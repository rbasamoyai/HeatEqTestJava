package rbasamoyai.heateq.cases;

import rbasamoyai.heateq.RodSimulation;

import java.util.Random;

public class Simulation3DFSCN {

    public static void run() {
        double dt = 1d / 20d;
        int steps = 100;

        int xDim = 20;
        int yDim = 20;
        int zDim = 20;

        long startTimeNanos = System.nanoTime();
        // initialize 3d cells with random values
        Random random = new Random();
        double valueScale = 100;
        double[][][] cellValues = new double[xDim][yDim][zDim];

        for (int xi = 0; xi < xDim; ++xi) {
            double[][] yzSlice = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi) {
                double[] zAxis = yzSlice[yi];
                for (int zi = 0; zi < zDim; ++zi)
                    zAxis[zi] = random.nextDouble() * valueScale;
            }
        }
        double[][][] cellOriginal = new double[xDim][yDim][zDim];
        for (int xi = 0; xi < xDim; ++xi) {
            double[][] yzSlice = cellOriginal[xi];
            double[][] yzSliceSrc = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi) {
                double[] zAxis = yzSlice[yi];
                double[] zAxisSrc = yzSliceSrc[yi];
                for (int zi = 0; zi < zDim; ++zi)
                    zAxis[zi] = zAxisSrc[zi];
            }
        }

        double diffScale = 80;
        double[][][] xCoeffRods = new double[yDim][zDim][xDim];
        double[][][] yCoeffRods = new double[xDim][zDim][yDim];
        double[][][] zCoeffRods = new double[xDim][yDim][zDim];

        // set up x-direction coefficient rods (indexed Y->Z, sized X)
        for (int yi = 0; yi < yDim; ++yi) {
            double[][] zxSlice = xCoeffRods[yi];
            for (int zi = 0; zi < zDim; ++zi) {
                double[] xRod = zxSlice[zi];
                for (int xi = 0; xi < xDim; ++xi)
                    xRod[xi] = random.nextDouble() * diffScale * dt * 0.5;
            }
        }
        // set up y-direction coefficient rods (indexed X->Z, sized Y) by copying from x-direction coefficient rods
        for (int xi = 0; xi < xDim; ++xi) {
            double[][] zySlice = yCoeffRods[xi];
            for (int zi = 0; zi < zDim; ++zi) {
                double[] yRod = zySlice[zi];
                for (int yi = 0; yi < yDim; ++yi)
                    yRod[yi] = xCoeffRods[yi][zi][xi];
            }
        }
        // set up z-direction coefficient rods (indexed X->Y, sized Z) by copying from x-direction coefficient rods
        for (int xi = 0; xi < xDim; ++xi) {
            double[][] yzSlice = zCoeffRods[xi];
            for (int yi = 0; yi < yDim; ++yi) {
                double[] zRod = yzSlice[yi];
                for (int zi = 0; zi < zDim; ++zi)
                    zRod[yi] = xCoeffRods[yi][zi][xi];
            }
        }

        // set up x-direction value rods (indexed Y->Z, sized X)
        double[][][] xValueRods = new double[yDim][zDim][xDim];
        for (int yi = 0; yi < yDim; ++yi) {
            double[][] zxSlice = xValueRods[yi];
            for (int zi = 0; zi < zDim; ++zi) {
                double[] xRod = zxSlice[zi];
                for (int xi = 0; xi < xDim; ++xi)
                    xRod[xi] = cellValues[xi][yi][zi];
            }
        }
        // set up y-direction rods (indexed X->Z, sized Y) and z-direction rods (indexed X->Y, sized Z)
        // no fill because the values are copied to each direction each tick (not efficient methinks)
        double[][][] yValueRods = new double[xDim][zDim][yDim];
        double[][][] zValueRods = new double[xDim][yDim][zDim];

        int maxDim = Math.max(Math.max(xDim, yDim), zDim);
        double[] rodBuf1 = new double[maxDim];
        double[] rodBuf2 = new double[maxDim];
        long endTimeNanos = System.nanoTime();
        long initTimeNanos = endTimeNanos - startTimeNanos;

        double initTime = initTimeNanos * 1e-9d;

        System.out.println();
        System.out.printf("Initialization time   : %16.10f s%n", initTime);

        // simulate 3d block
        startTimeNanos = System.nanoTime();
        for (int stepI = 0; stepI < steps; ++stepI) {
            // Start with x direction
            for (int yi = 0; yi < yDim; ++yi) {
                double[][] zxSliceT = xValueRods[yi];
                double[][] zxSliceK = xCoeffRods[yi];
                for (int zi = 0; zi < zDim; ++zi) {
                    double[] xRodT = zxSliceT[zi];
                    double[] xRodK = zxSliceK[zi];
                    RodSimulation.stepRodSimulation(xDim, xRodT, rodBuf1, rodBuf2, xRodK, dt);
                }
            }
            // Copy x to y rods
            for (int xi = 0; xi < xDim; ++xi) {
                double[][] zySlice = yValueRods[xi];
                for (int zi = 0; zi < zDim; ++zi) {
                    double[] yRod = zySlice[zi];
                    for (int yi = 0; yi < yDim; ++yi)
                        yRod[yi] = xValueRods[yi][zi][xi];
                }
            }
            // Then y direction
            for (int xi = 0; xi < xDim; ++xi) {
                double[][] zySliceT = yValueRods[xi];
                double[][] zySliceK = yCoeffRods[xi];
                for (int zi = 0; zi < zDim; ++zi) {
                    double[] yRodT = zySliceT[zi];
                    double[] yRodK = zySliceK[zi];
                    RodSimulation.stepRodSimulation(yDim, yRodT, rodBuf1, rodBuf2, yRodK, dt);
                }
            }
            // Copy y to z rods
            for (int xi = 0; xi < xDim; ++xi) {
                double[][] yzSlice = zValueRods[xi];
                for (int yi = 0; yi < yDim; ++yi) {
                    double[] zRod = yzSlice[yi];
                    for (int zi = 0; zi < zDim; ++zi)
                        zRod[zi] = yValueRods[xi][zi][yi];
                }
            }
            // Finally, z direction
            for (int xi = 0; xi < xDim; ++xi) {
                double[][] yzSliceT = zValueRods[xi];
                double[][] yzSliceK = zCoeffRods[xi];
                for (int yi = 0; yi < yDim; ++yi) {
                    double[] zRodT = yzSliceT[yi];
                    double[] zRodK = yzSliceK[yi];
                    RodSimulation.stepRodSimulation(zDim, zRodT, rodBuf1, rodBuf2, zRodK, dt);
                }
            }
            // Copy z to x
            for (int yi = 0; yi < yDim; ++yi) {
                double[][] zxSlice = xValueRods[yi];
                for (int zi = 0; zi < zDim; ++zi) {
                    double[] xRod = zxSlice[zi];
                    for (int xi = 0; xi < xDim; ++xi)
                        xRod[xi] = zValueRods[xi][yi][zi];
                }
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
        for (int xi = 0; xi < xDim; ++xi) {
            double[][] yzSlice = cellValues[xi];
            for (int yi = 0; yi < yDim; ++yi) {
                double[] zRod = yzSlice[yi];
                for (int zi = 0; zi < zDim; ++zi)
                    zRod[zi] = xValueRods[yi][zi][xi];
            }
        }
        int x = 0;
//
//        // Write to output
//        Path output = Path.of("run", "output2d.csv");
//        if (!Files.exists(output.getParent())) {
//            try {
//                Files.createDirectory(output.getParent());
//            } catch (IOException e) {
//                return;
//            }
//        }
//        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(output))) {
//            for (int yi = 0; yi < yDim; ++yi) {
//                String[] xRodStr = new String[xDim];
//                for (int xi = 0; xi < xDim; ++xi)
//                    xRodStr[xi] = Double.toString(cellOriginal[xi][yi]);
//                writer.writeNext(xRodStr, false);
//            }
//            writer.writeNext(new String[]{}, false);
//            for (int yi = 0; yi < yDim; ++yi) {
//                String[] xRodStr = new String[xDim];
//                for (int xi = 0; xi < xDim; ++xi)
//                    xRodStr[xi] = Double.toString(cellValues[xi][yi]);
//                writer.writeNext(xRodStr, false);
//            }
//            writer.writeNext(new String[]{}, false);
//            for (int yi = 0; yi < yDim; ++yi) {
//                String[] xRodStr = new String[xDim];
//                double[] xCoeffRod = xCoeffRods[yi];
//                for (int xi = 0; xi < xDim; ++xi)
//                    xRodStr[xi] = Double.toString(xCoeffRod[xi]);
//                writer.writeNext(xRodStr, false);
//            }
//        } catch (Exception e) {
//
//        }
    }

    private Simulation3DFSCN() {}

}

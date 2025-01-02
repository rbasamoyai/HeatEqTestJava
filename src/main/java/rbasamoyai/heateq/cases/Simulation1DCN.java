package rbasamoyai.heateq.cases;

import com.opencsv.CSVWriter;
import rbasamoyai.heateq.NumGenUtils;
import rbasamoyai.heateq.RodSimulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Simulation1DCN {

    public static void run() {
        double dt = 1d / 20d;
        int steps = 100;

        int length = 21;
        double[] xes = NumGenUtils.createArrayFromRange(length, x -> (double) x);

        double diffusivityScale = 10;
        double[] initialRod = NumGenUtils.createArrayFromOtherArray(xes, x -> x * ((double) length - x - 1));
        double[] rodDiff = NumGenUtils.createArrayFromOtherArray(xes, x -> (x + 1) / (double) length * diffusivityScale);
        double[] rodK = NumGenUtils.createArrayFromOtherArray(rodDiff, x -> x * dt * 0.5); // Precomputed coefficients

        double[] rodT = new double[length]; // Operated rod
        System.arraycopy(initialRod, 0, rodT, 0, length);
        double[] rodResult = new double[length]; // Buffer for explicit and implicit steps
        double[] impScratch = new double[length]; // Buffer for implicit step

        long startNanos = System.nanoTime();
        for (int stepIdx = 0; stepIdx < steps; ++stepIdx)
            RodSimulation.stepRodSimulation(rodT, rodResult, impScratch, rodK, dt);
        long endNanos = System.nanoTime();

        long simTimeNanos = endNanos - startNanos;
        double simTime = simTimeNanos * 1e-9d;

        System.out.println();
        System.out.printf("Simulation time       : %16.10f s%n", simTime);
        System.out.printf("Simulated time        : %16.10f s%n", dt * steps);
        System.out.printf("Average time per tick : %16.10f s%n", simTime / steps);
        System.out.printf("Time per tick to beat : %16.10f s%n", dt);

        // Write to output
        Path output = Path.of("run", "output1d.csv");
        if (!Files.exists(output.getParent())) {
            try {
                Files.createDirectory(output.getParent());
            } catch (IOException e) {
                return;
            }
        }
        try (CSVWriter writer = new CSVWriter(Files.newBufferedWriter(output))) {
            String[] originalLine = new String[length];
            for (int i = 0 ; i < length; ++i)
                originalLine[i] = Double.toString(initialRod[i]);
            String[] line = new String[length];
            for (int i = 0 ; i < length; ++i)
                line[i] = Double.toString(rodT[i]);
            writer.writeNext(originalLine, false);
            writer.writeNext(line, false);
        } catch (Exception e) {

        }
    }

    private Simulation1DCN() {}

}

package rbasamoyai.heateq;

public class RodSimulation {

    public static void init() {}

    public static void stepRodSimulation(int sz, double[] rodT, double[] buf1, double[] buf2, double[] rodK, double dt) {
        // Adapted from Cen, Hoppe, and Gu (2016)
        // Read more: https://pubs.aip.org/aip/adv/article/6/9/095305/882010/Fast-and-accurate-determination-of-3D-temperature

        // sz is the size of the rod to analyze. No length checks are done for faster performance.
        // rodT is the starting rod T(t=0)
        // buf1 is the buffer for both the explicit and implicit phases and writes back to rod_t
        // buf2 stores the modified superdiagonal values for the matrix calculations in the implicit step
        // rodK is a precomputed constants rod

        // explicit step
        buf1[0] = rodT[0] + (-2 * rodT[0] + rodT[1]) * rodK[0];
        buf1[sz - 1] = rodT[sz - 1] + (-2 * rodT[sz - 1] + rodT[sz - 2]) * rodK[sz - 1];
        for (int i = 1; i < sz - 1; ++i) {
            buf1[i] = rodT[i] + (rodT[i - 1] - 2 * rodT[i] + rodT[i + 1]) * rodK[i];
        }

        // implicit step - Thomas' algorithm
        // Adapted from: https://en.wikipedia.org/wiki/Tridiagonal_matrix_algorithm#Method
        buf2[0] = rodK[0] / (1 + 2 * rodK[0]);
        buf1[0] = buf1[0] / (1 + 2 * rodK[0]);

        for (int i = 1; i < sz - 1; ++i) {
            double k = rodK[i];
            double scr = buf2[i - 1];
            // a,c = -k
            // b = 1 + 2k
            double recip = 1 / (1 + (2 + scr) * k); // 1 / (b - a * scr)
            buf2[i] = -k * recip;
            buf1[i] = (buf1[i] + k * buf1[i - 1]) * recip;
        }
        buf1[sz - 1] = (buf1[sz - 1] + rodK[sz - 1] * buf1[sz - 2]) / (1 + (2 + buf2[sz - 2]) * rodK[sz - 1]);

        for (int i = sz - 2; i >= 0; --i)
            buf1[i] -= buf2[i] * buf1[i + 1];

        // Copy to rodT
        System.arraycopy(buf1, 0, rodT, 0, sz);
    }

    private RodSimulation() {}

}

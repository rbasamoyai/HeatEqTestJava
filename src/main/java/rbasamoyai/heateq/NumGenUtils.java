package rbasamoyai.heateq;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class NumGenUtils {

    public static double[] createArrayFromRange(int sz, Function<Integer, Double> func) {
        double[] arr = new double[sz];
        for (int i = 0; i < sz; ++i)
            arr[i] = func.apply(i);
        return arr;
    }

    public static double[] createArrayFromOtherArray(double[] src, UnaryOperator<Double> func) {
        double[] arr = new double[src.length];
        for (int i = 0; i < src.length; ++i)
            arr[i] = func.apply(src[i]);
        return arr;
    }

    private NumGenUtils() {}

}

package rbasamoyai.heateq.data;

public class MutableDouble {

    // Not looking to parallelize, so no care for concurrency right now...

    private double value;

    public MutableDouble(double value) {
        this.value = value;
    }

    public MutableDouble() { this(0d); }

    public void setValue(double value) { this.value = value; }
    public double getValue() { return this.value; }

}

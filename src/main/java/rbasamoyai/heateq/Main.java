package rbasamoyai.heateq;

import rbasamoyai.heateq.cases.Simulation1DCN;
import rbasamoyai.heateq.cases.Simulation2DFSCN;

public class Main {

    public static void main(String[] args) {
        RodSimulation.init(); // Just to avoid skewing the timer from classloading

        System.out.println("1D rod Crank-Nicolson");
        Simulation1DCN.run();

        System.out.println();

        System.out.println("2D rod fractional steps Crank-Nicolson");
        Simulation2DFSCN.run();
    }

}
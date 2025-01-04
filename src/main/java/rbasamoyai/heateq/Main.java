package rbasamoyai.heateq;

import rbasamoyai.heateq.cases.Simulation1DCN;
import rbasamoyai.heateq.cases.Simulation2DFSCN;
import rbasamoyai.heateq.cases.Simulation2DFSCNMutables;
import rbasamoyai.heateq.cases.Simulation3DFSCN;

public class Main {

    public static void main(String[] args) {
        RodSimulation.init(); // Just to avoid skewing the timer from classloading

        System.out.println("1D Crank-Nicolson");
        Simulation1DCN.run();

        System.out.println();
        System.out.println("2D fractional steps Crank-Nicolson using primitive variables");
        Simulation2DFSCN.run();

        System.out.println();
        System.out.println("2D fractional steps Crank-Nicolson using mutable variables");
        Simulation2DFSCNMutables.run();

        System.out.println();
        System.out.println("3D fractional steps Crank-Nicolson");
        Simulation3DFSCN.run();
    }

}
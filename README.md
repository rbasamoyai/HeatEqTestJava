# Heat Equation Test
by rbasamoyai

---

This code is a prototype for heat equation calculations in 1D, 2D, and 3D.
The goal is to eventually include this in *Create: Mother of All Reactors (MOAR)*,
an in-development *Minecraft* Java mod and addon for the *Create* mod. This
prototype is intended to analyze the potential computational performance of
the model and diagnose any code issues. This code is implemented in pure Java.

Because visualization is not implemented in the program, computed values are
written to .csv files that can be processed in other programs. The files can
be found in the `run/` folder generated during runtime.

---

This code is mainly based on work by Cen, Hoppe, and Gu (2016). You can find the
source article here: https://doi.org/10.1063/1.4962665
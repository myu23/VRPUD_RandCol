# Improving Column-Generation for Vehicle Routing Problems via Random Coloring and Parallelization
This repository contains the source code used in the computational experiments of paper: **Improving Column-Generation for Vehicle Routing Problems via Random Coloring and Parallelization** (most updated version is available at Optimization Online [http://www.optimization-online.org/DB_HTML/2021/03/8292.html]). 

In our paper, we solve Vehicle Routing Problem with Unit Demand using column generation approach. We promote a random coloring algorithm that solves the elmentary shortest path problem with resource constraints (ESPPRC), which serves as the subproblem of column generation-based approach.

## Prerequisites

Before you continue, ensure you have met the following requirements:
* You have access of Gurobi. It is used as the optimization solver for various linear programming and integer programming models in the code.
* You have a basic understanding of column generation approach.

## Test Instances
There are three classes of test instances included in the computational experiments:
* Modified VRPTW Instances 
* CVRP X-Instances
* Medical Home Delivery in Wayne County Intances (multi-depot)

They are provided in the folder `/Instances`

## How to Use
One can create a project and use the code of this repository in Java IDE by selecting "create new project with existing sources". However, the user need to have Gurobi solver installed and add it as global library to this project.

The driver of the computation is `src/RandCol/NumericalTests`. However, it only serves as an example of how to call the solver. The code may need to be modified based on the operating systems. 

The original source code of pulse algorithm is under `src/Pulse` and the code for our random coloring algorithm is under `src/RandCol`.

## Acknowledgement 
We thank Dr. Leonardo Lozano for providing to us the code used in their paper Lozano et al. (2015).

//package pulse;
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
//* This is the main class for the ESPPRC pulse algorithm.
//
// * Ref.: Lozano, L., Duque, D., and Medaglia, A. L. (2013).
//
// * An exact algorithm for the elementary shortest path problem with resource constraints. Technical report COPA 2013-2
//
// * @author L. Lozano & D. Duque
//
// * @affiliation Universidad de los Andes - Centro para la Optimization y Probabilidad Aplicada (COPA)
//
// * @url http://copa.uniandes.edu.co/
//
// */
//
//public class Main {
//
//	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
//
//		// Data file information
//		String dataFile = null;
//		String dir = "Solomon Instances/";
//		String instanceType = "C";
//		int instanceNumber = 101;
//		String extension = ".txt";
//		dataFile = dir + instanceType + instanceNumber + extension;
//		System.out.println("Instance: "+dataFile);
//
//		// Read data file and define the following parameters: number of threads, number of nodes, and step size for the bounding procedure
//		int numThreads = 1;
//		int numNodes = 100;
//		int stepSize = 10;
//		DataHandler data = new DataHandler(dataFile, instanceType, instanceNumber, numThreads, stepSize);
//		data.readSolomon(numNodes);
//
//		// Generate an ESPPRC instance with dual variables taken from an iteration of the CG (only available for the R-200 series!)
//		data.generateInstance(instanceNumber);
//
//////////////////////////////////////////////////// BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////
//		long tNow = System.currentTimeMillis(); 							// Measure current execution time
//
//		GraphManager.calNaiveDualBound();									// Calculate a naive lower bound
//		GraphManager.timeIncumbent=GraphManager.nodes[0].tw_b;				// Capture the depot upper time window
//		int lowerTimeLimit = 100; 											// Lower time (resource) limit to stop the bounding procedure. For 100-series we used 50 and for 200-series we used 100;
//		int timeIndex=0;													// Index to store the bounds
//
//		while(GraphManager.timeIncumbent>=lowerTimeLimit){					// Check the termination condition
//
//			timeIndex=(int) Math.ceil((GraphManager.timeIncumbent/DataHandler.boundStep));		// Calculate the current index
//
//			for (int i = 1; i <= DataHandler.n; i++) {
//				GraphManager.nodes[i].pulseBound(0, GraphManager.timeIncumbent, 0 , new ArrayList(), i,0); 	// Solve an ESPPRC for all nodes given the time incumbent
//			}
//
//			for(int i=1; i<=DataHandler.n; i++){
//				GraphManager.boundsMatrix[i][timeIndex]=GraphManager.bestCost[i];				// Store the best cost found for each node into the bounds matrix
//			}
//
//			GraphManager.overallBestCost=GraphManager.PrimalBound;					// Store the best cost found over all the nodes
//			GraphManager.timeIncumbent-=DataHandler.boundStep;						// Update the time incumbent
//		}
//
//
//////////////////////////////////////////////////END OF BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////
//		// Run pulse
//		GraphManager.timeIncumbent+=DataHandler.boundStep; 				// Set time incumbent to the last value solved
//		GraphManager.PrimalBound=0;										// Reset the primal bound
//
//		GraphManager.nodes[0].pulseMT(0, 0, 0, new ArrayList(),0,0); 	// Run the pulse procedure on the source node
//
//
//		// Print results
//
//		long time = (long) ((System.currentTimeMillis()-tNow));			// Calculate execution time
//
//		System.out.println("Execution time: "+time/1000.0+" seconds\n");
//
//		System.out.println("************ OPTIMAL SOLUTION *****************\n");
//		System.out.println("Optimal cost: "+GraphManager.finalNode.PathCost);
//		System.out.println("Optimal time: "+GraphManager.finalNode.PathTime);
//		System.out.println("Optimal Load: "+GraphManager.finalNode.PathLoad);
//		System.out.println();
//		System.out.println("Optimal path: ");
//		System.out.println(GraphManager.finalNode.Path);
//
//	}
//}

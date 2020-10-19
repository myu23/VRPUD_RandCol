/**

* This class stores all the data and reads the input data file 

 * Ref.: Lozano, L., Duque, D., and Medaglia, A. L. (2013). 

 * An exact algorithm for the elementary shortest path problem with resource constraints. Technical report COPA 2013-2  

 * @author L. Lozano & D. Duque

 * @affiliation Universidad de los Andes - Centro para la Optimizacin y Probabilidad Aplicada (COPA)

 * @url http://copa.uniandes.edu.co/

 **/

package pulse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import miaoyu.helper.DataMD;
import miaoyu.helper.Data;


public class DataHandler {
	
	// Graph attributes
	public static int numArcs;			//Number of arcs
	public static int n;				//Number of nodes
	static int lastNode;				//Last node
	public static int Q;				//Capacity limit

	public static int[][] arcs;			// All the arcs of the network stored in a vector where arcs[i][0] = tail of arc i and arcs[i][1] = head of arc i 
	public static double[] distList;	// Distance attribute for any arc i 
	public static double[] timeList;	// Time attribute for any arc i
	public static double[] costList;	// Cost attribute for any arc i
	public static double[] loadList;	// Load attribute for any arc i
	public static double[][] distance;	// Distance matrix
	public static double[][] cost;		// Cost matrix
	
	public static int[] demand;			// Demand for each node
	public static int[] service;		// Service duration for each node
	public static int[] tw_a;			// Lower time window for each node
	public static int[] tw_b;			// Upper time window for each node
	
	public static double[] x;			// x coordinate for each customer 
	public static double[] y;			// y coordinate for each customer
		
	private GraphManager G;				// Data structure that holds the graph
	public static final double R = 6372.8; // In kilometers

	// Input file information
	private String  instanceType;
	private int instanceNumber;
	public static String CvsInput;
	
	// Other stuff
	public static int numThreads;		// Number of threads
	public static Thread[] threads;		// Threads
	public static int boundStep;		// Step size for the bounding procedure
	public static double[] pi;			// dual variables
	public static Random r;				// Random numbers generator 
	public static int root;
	public static DataMD data;
	public static Data d;

	public DataHandler(String dataFile, String type , int number, int nThreads, int step) {
		CvsInput = dataFile;
		instanceType = type;
		instanceNumber=number;
		numThreads = nThreads;
		boundStep = step;

		threads = new Thread[DataHandler.numThreads+1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread();
		}
	}

	public DataHandler(Data data, int nThreads, int step) {
		this.d = data;
		this.root = root;
//		instanceType = type;
//		instanceNumber=number;
		numThreads = nThreads;
		boundStep = step;

		threads = new Thread[DataHandler.numThreads+1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread();
		}
	}
	public DataHandler(DataMD data, int root,  int nThreads, int step) {
		this.data = data;
		this.root = root;
//		instanceType = type;
//		instanceNumber=number;
		numThreads = nThreads;
		boundStep = step;

		threads = new Thread[DataHandler.numThreads+1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread();
		}
	}

	
	
	// Generates random dual variables to build a random subproblem

	public void generateInstance(double[] dual){
		this.pi = dual;
		for (int i = 0; i < numArcs; i++) {
			costList[i] = distList[i]-pi[arcs[i][0]]; //Calculate reduced cost with the dual variable of the tail node of each arc
		}

		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {

				cost[i][j]= distance[i][j]-pi[i]; //Calculate reduced cost with the dual variable of the tail node of each arc
			}
		}
	}
	/**
	 * Read a Solomon instance
	 * @throws NumberFormatException
	 * @throws IOException
	 */

	public void readSolomon(int numNodes, int q) throws NumberFormatException, IOException {


		File file = new File(CvsInput);
		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		String line = bufRdr.readLine(); //READ Num Nodes
		this.Q = q;

		n = numNodes;

		x = new double[n+1];
		y = new double[n+1];
		demand = new int[n+1];
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];

		String[] stringReader = new String[7];
		int indexString = 0;
		stringReader = line.trim().split("\\s+");



		x[0] =Double.parseDouble(stringReader[1]);
		y[0] =Double.parseDouble(stringReader[2]);
		service[0] = (int)(Double.parseDouble(stringReader[6]));
		demand[0]=(int)(Double.parseDouble(stringReader[3]));
		tw_a[0]= (int)(Double.parseDouble(stringReader[4]));
		tw_b[0]= (int)(Double.parseDouble(stringReader[5]));
		G = new GraphManager(n+1);
		int auxNumArcs = (n+1)*(n+1)-(n+1);
		G.addVertex(new Node(0,demand[0],service[0],-tw_b[0],tw_b[0]));
		int customerNumber = 1;

		while (customerNumber<=n) {
			indexString=0;
			stringReader= new String[7];
			line = bufRdr.readLine();
			stringReader = line.trim().split("\\s+");

			x[customerNumber] =Double.parseDouble(stringReader[1]);
			y[customerNumber] =Double.parseDouble(stringReader[2]);
			service[customerNumber] = 0;//(int)(Double.parseDouble(stringReader[6]));
			demand[customerNumber]=1;//(int)(Double.parseDouble(stringReader[3]));
			tw_a[customerNumber]= 0;//(int)(Double.parseDouble(stringReader[4]));
			tw_b[customerNumber]= tw_b[0];//(int)(Double.parseDouble(stringReader[5]));

			G.addVertex(new Node(customerNumber,demand[customerNumber],service[customerNumber], tw_a[customerNumber],tw_b[customerNumber]));
			customerNumber++;
		}


		distance = new double[n + 1][n + 1];
		cost = new double[n + 1][n + 1];
		distList = new double[auxNumArcs];
		costList = new double[auxNumArcs];
		loadList = new double[auxNumArcs];
		timeList = new double[auxNumArcs];
		arcs = new int[auxNumArcs][2];
		int arc = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {
				// Redondio a un decimal pero est� x10 para que quede entero
				// para el SP
				double d_ij = Math.sqrt(Math.pow((x[i] - x[j]), 2)	+ Math.pow((y[i] - y[j]), 2));
				double dINT = Math.floor(d_ij*10)/10;
				distance[i][j] = dINT;
				distance[j][i] = dINT;
//				if(j==n){
//					distance[i][j] = 0;
//				}

				cost[i][j] = dINT ;
				cost[j][i] = dINT;


				// PODAR CON TW
				if ((i==0 && (i!=j))  ||((i!=j) && tw_a[i] + service[i] + dINT <= tw_b[j]) ) {
					distList[arc] = dINT;
					costList[arc] = cost[i][j];
					arcs[arc][0] = i;
					arcs[arc][1] = j;
					timeList[arc] = dINT + service[i];
					loadList[arc] = demand[j];
					int a1 = arc;
					G.nodes[i].magicIndex.add(a1);

					arc++;
				}
			}
		}

		numArcs =arc;

		for (int i = 0; i < n; i++) {
			G.getNodes()[i].autoSort();
		}

	}



	public void readMed(int numNodes, int q) throws NumberFormatException, IOException {

		

		this.Q = q;
		
		n = numNodes; 

		x = new double[n+1]; 
		y = new double[n+1]; 
		demand = new int[n+1]; 
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];
		
		x[0] = data.coor_x[root];
		y[0] = data.coor_y[root];
		service[0] = 0;
		demand[0]=0;
		tw_a[0]= 0;
		tw_b[0]= 1000;
		G = new GraphManager(n+1); 
		int auxNumArcs = (n+1)*(n+1)-(n+1);
		G.addVertex(new Node(0,demand[0],service[0],-tw_b[0],tw_b[0]));
		int customerNumber = 1;
	
		while (customerNumber<=n) {
			x[customerNumber] = data.coor_x[data.dDepot+customerNumber-1];
			y[customerNumber] = data.coor_y[data.dDepot+customerNumber-1];

			service[customerNumber] = 0;//(int)(Double.parseDouble(stringReader[6]));
			demand[customerNumber]=1;//(int)(Double.parseDouble(stringReader[3]));
			tw_a[customerNumber]= 0;//(int)(Double.parseDouble(stringReader[4]));
			tw_b[customerNumber]= tw_b[0];//(int)(Double.parseDouble(stringReader[5]));
			
			G.addVertex(new Node(customerNumber,demand[customerNumber],service[customerNumber], tw_a[customerNumber],tw_b[customerNumber]));
			customerNumber++;
		}
		
		
		distance = new double[n + 1][n + 1];
		cost = new double[n + 1][n + 1];
		distList = new double[auxNumArcs];
		costList = new double[auxNumArcs];
		loadList = new double[auxNumArcs];
		timeList = new double[auxNumArcs];
		arcs = new int[auxNumArcs][2];
		int arc = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {
				// Redondio a un decimal pero est� x10 para que quede entero
				// para el SP
				double dINT = haversine(i,j);
				distance[i][j] = dINT;
				distance[j][i] = dINT;
//				if(j==n){
//					distance[i][j] = 0;
//				}
				
				cost[i][j] = dINT ;
				cost[j][i] = dINT;


				// PODAR CON TW
				if ((i==0 && (i!=j))  ||((i!=j) && tw_a[i] + service[i] + dINT <= tw_b[j]) ) {
					distList[arc] = dINT;
					costList[arc] = cost[i][j];
					arcs[arc][0] = i;
					arcs[arc][1] = j;
					timeList[arc] = dINT + service[i];
					loadList[arc] = demand[j];
					int a1 = arc;
					G.nodes[i].magicIndex.add(a1);
					
					arc++;
				}
			}
		}
		
		numArcs =arc;
		
		for (int i = 0; i < n; i++) {
			G.getNodes()[i].autoSort();
		}
	
	}
	public void readX(int numNodes, int q) throws NumberFormatException, IOException {



		this.Q = q;

		n = numNodes;

		x = new double[n+1];
		y = new double[n+1];
		demand = new int[n+1];
		service =  new int[n+1];
		tw_a =  new int[n+1];
		tw_b =  new int[n+1];

		x[0] = d.coor_x[0];
		y[0] = d.coor_y[0];
		service[0] = 0;
		demand[0]=0;
		tw_a[0]= 0;
		tw_b[0]= 5000;
		G = new GraphManager(n+1);
		int auxNumArcs = (n+1)*(n+1)-(n+1);
		G.addVertex(new Node(0,demand[0],service[0],-tw_b[0],tw_b[0]));
		int customerNumber = 1;

		while (customerNumber<=n) {
			x[customerNumber] = d.coor_x[customerNumber];
			y[customerNumber] = d.coor_y[customerNumber];

			service[customerNumber] = 0;//(int)(Double.parseDouble(stringReader[6]));
			demand[customerNumber]=1;//(int)(Double.parseDouble(stringReader[3]));
			tw_a[customerNumber]= 0;//(int)(Double.parseDouble(stringReader[4]));
			tw_b[customerNumber]= tw_b[0];//(int)(Double.parseDouble(stringReader[5]));

			G.addVertex(new Node(customerNumber,demand[customerNumber],service[customerNumber], tw_a[customerNumber],tw_b[customerNumber]));
			customerNumber++;
		}


		distance = new double[n + 1][n + 1];
		cost = new double[n + 1][n + 1];
		distList = new double[auxNumArcs];
		costList = new double[auxNumArcs];
		loadList = new double[auxNumArcs];
		timeList = new double[auxNumArcs];
		arcs = new int[auxNumArcs][2];
		int arc = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= n; j++) {
				// Redondio a un decimal pero est� x10 para que quede entero
				// para el SP
				double dINT = dist_i(i,j);
				distance[i][j] = dINT;
				distance[j][i] = dINT;
//				if(j==n){
//					distance[i][j] = 0;
//				}

				cost[i][j] = dINT ;
				cost[j][i] = dINT;


				// PODAR CON TW
				if ((i==0 && (i!=j))  ||((i!=j) && tw_a[i] + service[i] + dINT <= tw_b[j]) ) {
					distList[arc] = dINT;
					costList[arc] = cost[i][j];
					arcs[arc][0] = i;
					arcs[arc][1] = j;
					timeList[arc] = dINT + service[i];
					loadList[arc] = demand[j];
					int a1 = arc;
					G.nodes[i].magicIndex.add(a1);

					arc++;
				}
			}
		}

		numArcs =arc;

		for (int i = 0; i < n; i++) {
			G.getNodes()[i].autoSort();
		}

	}

//private void readCapacity() throws IOException {
//		File file = new File("Solomon Instances/capacities.txt");
//		BufferedReader bufRdr = new BufferedReader(new FileReader(file));
////		for (int i = 0; i < 6; i++) {
////			String line = bufRdr.readLine(); //READ Num Nodes
////			String[] spread = line.split(":");
////			if(instanceType.equals(spread[0])){
////				int serie = Integer.parseInt(spread[1]);
////				if (instanceNumber-serie<50) {
////					Q=Integer.parseInt(spread[2]);
////					return;
////				}else{
////					Q=200;
////					return;
////				}
////			}
////		}
//		Q=4;
//		return;
//
//
//
//	}

	public double haversine(int i, int j) {
		double lat1 = x[i];
		double lon1 = y[i];
		double lat2 = x[j];
		double lon2 = y[j];
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return (R * c)+1E-6;
//		double result = Math.floor(((R * c)+1E-6)*10)/10;
//		return result;
	}

	public double dist_i(int i , int j){
		return Math.floor(Math.sqrt(Math.pow(x[i]-x[j], 2) + Math.pow(y[i] - y[j], 2))+0.5);
	}

}

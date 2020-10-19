/**
 * This class contains the column generation solver to BCP for VRPCC.
 * @author Miao Yu
 * @since May 6, 2017
 *
 */
package miaoyu.algorithm;

import gurobi.*;
import gurobi.GRB.DoubleAttr;
import miaoyu.helper.Data;
import miaoyu.helper.DataMD;
import miaoyu.helper.Functions;
import miaoyu.helper.Route;
import pulse.DataHandler;
import pulse.GraphManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ColumnGeneration_RCMDe {
	public DataMD da;
	public boolean isStrongBranching;
	public double lowerbound;
	public double upperbound;
	public boolean initialized;
	public ArrayList<ArrayList<Route>> routes;
	public ArrayList<Route> artificialRoute;
	public ArrayList<Route> solRoutes;
	private int nNode;
	private int nVehicle;
	public int nIter;
	public int nColumns;
	public long timer;
	public long timerBeforeLast = 0;
	public static long count_bound=0;
	public static long count_depth_bound=0;
	public static long count_inf=0;
	public static long count_roll=0;
	public long lptime;
	public long iptime;
	public String fileName;
	public long etime=0;
	public int count=0;

	public ColumnGeneration_RCMDe(DataMD d){
		this.da = d;
		this.nNode = d.nNode;
		this.nVehicle = d.nVehicle;
		this.initialized = false;
		this.solRoutes = new ArrayList<>();
		this.routes = new ArrayList<>();
		for(int i = 0; i < d.nDepot; i++){
			this.routes.add(new ArrayList<>());
		}
		this.nIter = 0;
		this.nColumns = 0;
		this.timer = System.currentTimeMillis();
		System.out.println("constructed");
	}

	/**
	 * Depreciated
	 *
	 * @return
	 */
	public double solve()throws GRBException, InterruptedException, NumberFormatException, IOException {
		int i, j, k;
		try {

			String logName = "log\\Medical\\LP";
			File f = new File(logName);
			f.delete();
			GRBEnv   env   = new GRBEnv(logName);
			GRBModel master = new GRBModel(env);
			master.set(GRB.IntParam.Threads, 39);


			//double cost;
			//update cost of routes
			for(int d = 0; d < da.nDepot; d++){
				for (Route r : routes.get(d)) {
					r.updateCost(da.distance[d]);
			    }
			}
			System.out.println("updated");


			// complete the lp with basic route to ensure feasibility
			int tot_size = 0;
			for(int d = 0; d <da.nDepot; d++)
				tot_size += routes.get(d).size();


			if (tot_size < 1) { //execute only the first time
				System.out.println("Initialize!");
					for(int d = 0; d < da.nDepot; d++){
						for(i = 1; i < nNode; i++){
							ArrayList<Integer> temp = new ArrayList<Integer>(1);
							temp.add(i);
							Route tempRoute = new Route(temp);
							tempRoute.updateCost(da.distance[d]);
							routes.get(d).add(tempRoute);
						}
					}

			}

			//routes = savings(routes);
			for(int d = 0; d < da.nDepot; d++){
				System.out.println("original size: "+routes.get(d).size());
				System.out.println("saving size: "+ savings(routes.get(d), d).size());
				routes.get(d).addAll(savings(routes.get(d),d));
				//routes.set(d, savings(routes.get(d), d));
				for (Route r : routes.get(d)) {
					r.updateCost(da.distance[d]);
				}
			}
			// create variables
			GRBVarArray[] lambda = new GRBVarArray[da.nDepot];
			for(int d = 0; d < da.nDepot; d++){
				lambda[d] = new GRBVarArray();
			}

			for(int d = 0; d < da.nDepot; d++){
				for(int p = 0; p < routes.get(d).size(); p++){
					lambda[d].add(master.addVar(0, 1, 0, GRB.CONTINUOUS, "lambda"+p));
				}
			}


			// Add constraints : (2.7)
			GRBLinExpr expr = new GRBLinExpr();

				expr = new GRBLinExpr();
				for(int d = 0; d < da.nDepot; d++){
					for(int p = 0; p < routes.get(d).size();p++){
						expr.addTerm(routes.get(d).get(p).cost, lambda[d].getElement(p));
						//System.out.println("v"+k+": "+routes[k].get(p).getCost());
					}
				}

			master.setObjective(expr, GRB.MINIMIZE);


			// Integrate new variables

			master.update();


			// Add visit constraint
			GRBConstr[] cons_visit = new GRBConstr[nNode];
			for(j = 1; j < nNode; j++){
				expr = new GRBLinExpr();
				for(int d = 0 ;d < da.nDepot;d++){
					for(int p = 0; p < routes.get(d).size();p++){
						if(routes.get(d).get(p).contains(j))
							expr.addTerm(1, lambda[d].getElement(p));
					}
				}

				cons_visit[j] = master.addConstr(expr, GRB.GREATER_EQUAL, 1, "c1"+j);
			}



			Data data = new Data("Solomon_100//C161.txt", da.nNode,da.capacity);

			boolean oncemore = true;
			double objval = Double.MAX_VALUE;
			timer = System.currentTimeMillis();
			lptime = System.currentTimeMillis();
			while(oncemore){
				oncemore = false;
				nIter++;
				master.optimize();
				// print master status
				int optimstatus = master.get(GRB.IntAttr.Status);
				if (optimstatus == GRB.Status.OPTIMAL) {
					objval = master.get(DoubleAttr.ObjVal);
					System.out.println("Current Optimal objective: " + objval);
				} else{
					System.out.println("CG: relaxation infeasible!");
					return 1E10;
				}

				if(System.currentTimeMillis() - timer > 28800000) {
					lowerbound = objval;
					return objval;
				}
				double[] alpha = new double[nNode];
				//double[] gamma = new double[K];
				for(i = 1; i < nNode; i++){
					alpha[i] = cons_visit[i].get(DoubleAttr.Pi);
				}

				System.out.println("print dual solutions");
				System.out.println("alpha");
				System.out.println(Arrays.toString(alpha));

				//				if(oncemore == false)
				//					break;
				// update cost for each pricing problem
				System.out.println("Solving pricing problem");


				//					System.out.println("printing the updated cost matrix for vehicle "+k);
				//					for(i = 0; i < nVehicle; i++){
				//						System.out.println(Arrays.toString(data.cost[i]));
				//					}
				//Data data = new Data("Solomon_100//C161.txt", da.nNode,da.capacity);
				for(int d = 0; d < da.nDepot; d++){
					data.distance = da.distance[d];
					for (i = 1; i < nNode; i++){
						if(data.distance[0][i] > 1000 ){
							data.cost[0][i] = data.distance[0][i];
						}else{
							data.cost[0][i] = data.distance[0][i] - alpha[i];
						}
					}
					for (j = 0; j < nNode; j++){
						if(data.distance[j][nNode] > 1000 ){
							data.cost[j][nNode] = data.distance[j][0];
						}else{
							data.cost[j][nNode] = data.distance[j][0];
						}
						//data.cost[u][0] = data.vDistanceMatrix.get(k)[u][0];
						for (i = 0; i < nNode; i++){
							if(data.distance[j][i] > 1000 ){
								data.cost[j][i] = data.distance[j][i];
							}else{
								data.cost[j][i] = data.distance[j][i] - alpha[i];
							}
						}
					}

					data.setThread(39);
					RandColW pprc = new RandColW(data);
					Set<Route>newroute = new HashSet<Route>();

					if(pprc.solve()) {
						oncemore = true;
						newroute = pprc.solutionSet;
						System.out.println("number of new route found:" + newroute.size());
						for (Route r : newroute) {
							GRBColumn col = new GRBColumn();
							for (int v : r.route) {
								System.out.print(v + ",");
								col.addTerm(1, cons_visit[v]);
							}
							r.updateCost(data.distance);
							GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col, "lambda");
							lambda[d].add(newvar);
							routes.get(d).add(r);
							nColumns++;
						}
						timerBeforeLast = System.currentTimeMillis() - timer;
					}
				}


				master.update();

				if (!oncemore){

					for(int d = 0; d < da.nDepot; d++){
						Set<Route>newroute = new HashSet<Route>();

						// Read data file and define the following parameters: number of threads, number of nodes, and step size for the bounding procedure
						int numThreads = 39;
						int numNodes = da.nNode-1;
						int stepSize = 1;
						System.out.println("here!");
						DataHandler dh = new DataHandler(da, d,  numThreads, stepSize);
						dh.readMed(numNodes, da.capacity);

						// Generate an ESPPRC instance with dual variables taken from an iteration of the CG (only available for the R-200 series!)
						dh.generateInstance(alpha);
						System.out.println("done load");
////////////////////////////////////////////////// BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////
						long tNow = System.currentTimeMillis(); 							// Measure current execution time

						GraphManager.calNaiveDualBound();									// Calculate a naive lower bound
						GraphManager.capIncumbent=dh.Q;				// Capture the depot upper time window
						int lowerCapLimit = 2; 											// Lower time (resource) limit to stop the bounding procedure. For 100-series we used 50 and for 200-series we used 100;
						int capIndex=0;													// Index to store the bounds
						//System.out.println("initialize");
						while(GraphManager.capIncumbent>=lowerCapLimit){					// Check the termination condition

							capIndex=(int) Math.ceil((GraphManager.capIncumbent/DataHandler.boundStep));		// Calculate the current index
							for (int x = 1; x <= DataHandler.n; x++) {
								//GraphManager.nodes[x].pulseBound(0, GraphManager.timeIncumbent, 0 , new ArrayList(), x,0); 	// Solve an ESPPRC for all nodes given the time incumbent
								GraphManager.nodes[x].pulseMTBound(GraphManager.capIncumbent, 0, 0 , new ArrayList(), x,0,0); 	// Solve an ESPPRC for all nodes given the time incumbent

							}

							for(int x=1; x<=DataHandler.n; x++){
								GraphManager.boundsMatrix[x][capIndex]=GraphManager.bestCost[x];				// Store the best cost found for each node into the bounds matrix
							}
							GraphManager.overallBestCost=GraphManager.PrimalBound;					// Store the best cost found over all the nodes
							GraphManager.capIncumbent-=DataHandler.boundStep;						// Update the time incumbent
						}

						System.out.println("here!");
						System.out.println(Arrays.toString(GraphManager.boundsMatrix[0]));
////////////////////////////////////////////////END OF BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////

						// Run pulse
						GraphManager.capIncumbent+=DataHandler.boundStep; 				// Set time incumbent to the last value solved
						GraphManager.PrimalBound=0;										// Reset the primal bound
						GraphManager.finalNode.Path = new ArrayList();

						GraphManager.nodes[0].pulseMT(0, 0, 0, new ArrayList(),0,0); 	// Run the pulse procedure on the source node


						// Print results

						long time = (long) ((System.currentTimeMillis()-tNow));			// Calculate execution time

						System.out.println("Execution time: "+time/1000.0+" seconds\n");

						System.out.println("************ OPTIMAL SOLUTION *****************\n");
						System.out.println("Optimal cost: "+GraphManager.finalNode.PathCost);
						System.out.println("Optimal time: "+GraphManager.finalNode.PathTime);
						System.out.println("Optimal Load: "+GraphManager.finalNode.PathLoad);
						System.out.println();
						System.out.println("Optimal path: ");
						System.out.println(GraphManager.finalNode.Path);

						if(GraphManager.finalNode.PathCost < -1E-6){
							oncemore = true;
							for(int s = 0; s < GraphManager.paths.size(); s++){
								ArrayList<Integer> rls = new ArrayList<>();
								System.out.println(GraphManager.paths.get(s).toString());
								for(i = 1; i < GraphManager.paths.get(s).size() - 1; i++){
									rls.add((Integer) GraphManager.paths.get(s).get(i));
								}
								Route r = new Route(rls);
								System.out.println("number of new route found:" + newroute.size());
								GRBColumn col = new GRBColumn();
								for(int v : r.route){
									System.out.print(v + ",");
									col.addTerm(1, cons_visit[v]);
								}
								r.updateCost(da.distance[d]);
								//							System.out.println("Path for "+k+": "+Arrays.toString(r.route.toArray())+" "+r.cost);
								//							System.out.println(col);
								GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col,"lambda");
								lambda[d].add(newvar);
								routes.get(d).add(r);
								nColumns += 1;
							}




						}

					}


					master.update();
				}
			}
			lptime = System.currentTimeMillis() - lptime;
			lowerbound = master.get(DoubleAttr.ObjVal);
			upperbound = getUpperbound();
			etime = System.currentTimeMillis();
			count = routes.size();
			/****
			 * Enumeration!
			 */
			{
				double[] alpha = new double[nNode];
				//double[] gamma = new double[K];
				for (i = 1; i < nNode; i++) {
					alpha[i] = cons_visit[i].get(DoubleAttr.Pi);
				}

				System.out.println("print dual solutions");
				System.out.println("alpha");
				System.out.println(Arrays.toString(alpha));

				//				if(oncemore == false)
				//					break;
				// update cost for each pricing problem
				System.out.println("Solving pricing problem");


				//					System.out.println("printing the updated cost matrix for vehicle "+k);
				//					for(i = 0; i < nVehicle; i++){
				//						System.out.println(Arrays.toString(data.cost[i]));
				//					}
				//Data data = new Data("Solomon_100//C161.txt", da.nNode,da.capacity);
				for (int d = 0; d < da.nDepot; d++) {
					data.distance = da.distance[d];
					for (i = 1; i < nNode; i++) {
						if (data.distance[0][i] > 1000) {
							data.cost[0][i] = data.distance[0][i];
						} else {
							data.cost[0][i] = data.distance[0][i] - alpha[i];
						}
					}
					for (j = 0; j < nNode; j++) {
						if (data.distance[j][nNode] > 1000) {
							data.cost[j][nNode] = data.distance[j][0];
						} else {
							data.cost[j][nNode] = data.distance[j][0];
						}
						//data.cost[u][0] = data.vDistanceMatrix.get(k)[u][0];
						for (i = 0; i < nNode; i++) {
							if (data.distance[j][i] > 1000) {
								data.cost[j][i] = data.distance[j][i];
							} else {
								data.cost[j][i] = data.distance[j][i] - alpha[i];
							}
						}
					}

					data.setThread(39);
					RandColEnum pprc = new RandColEnum(data);
					pprc.setEpsilon(upperbound, lowerbound);
					Set<Route> newroute = new HashSet<Route>();
					long localt = System.currentTimeMillis();
					int temp = routes.get(d).size();
					if (pprc.solve()) {
						newroute = pprc.solutionSet;
						System.out.println("number of new route found:" + newroute.size());
						for (Route r : newroute) {
							r.updateCost(data.distance);
							routes.get(d).add(r);
						}
					}
					count += routes.get(d).size() - temp;
				}

				System.out.println("Column enumeration done!");
			}
			etime = System.currentTimeMillis() - etime;
			// Dispose of model and environment
			master.dispose();
			env.dispose();

			String filename = "GeneratedRoutes\\random_color"+da.nNode+"-"+da.nDepot;
			Functions.saveRoutes(filename, routes);
			return objval;

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
					e.getMessage());
			e.printStackTrace();
		}

		return 1E10;
	}

	public double getUpperbound(){
		ArrayList<String[]> rawData = new ArrayList<String[]>();
		String csvFile = "Data\\UCVRP4\\RCMD\\"+da.nDepot+"-"+da.nNode+"-ii.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		double ub = 0;
		try{
			br = new BufferedReader(new FileReader(csvFile));
			for(int i = 0; i < 6; i++)
				line = br.readLine();
			ub = Double.parseDouble(line);
			return ub+0.5;
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	public double solveMIP()throws InterruptedException, NumberFormatException, IOException {
		int i, j, k;
		try {
			String logName = "log\\Medical\\IP_n"+da.nNode+"d"+da.nDepot;
			File f = new File(logName);
			f.delete();
			GRBEnv   env   = new GRBEnv();
			GRBModel master = new GRBModel(env);
			master.set(GRB.IntParam.Threads, 39);

			master.set(GRB.DoubleParam.TimeLimit, 1800);

			//double cost;
			//update cost of routes
			for(int d = 0; d < da.nDepot; d++){
				for (Route r : routes.get(d)) {
					r.updateCost(da.distance[d]);
				}
			}


			// complete the lp with basic route to ensure feasibility
			int tot_size = 0;
			tot_size = routes.size();


			//routes = savings(routes);
			for(int d = 0; d < da.nDepot; d++){
				routes.set(d, savings(routes.get(d), d));
				for (Route r : routes.get(d)) {
					r.updateCost(da.distance[d]);
				}
			}
			// create variables
			GRBVarArray[] lambda = new GRBVarArray[da.nDepot];
			for(int d = 0; d < da.nDepot; d++){
				lambda[d] = new GRBVarArray();
			}

			for(int d = 0; d < da.nDepot; d++){
				for(int p = 0; p < routes.get(d).size(); p++){
					lambda[d].add(master.addVar(0, 1, 0, GRB.BINARY, "lambda"+p));
				}
			}


			// Add constraints : (2.7)
			GRBLinExpr expr = new GRBLinExpr();

			expr = new GRBLinExpr();
			for(int d = 0; d < da.nDepot; d++){
				for(int p = 0; p < routes.get(d).size();p++){
					expr.addTerm(routes.get(d).get(p).cost, lambda[d].getElement(p));
				}
			}

			master.setObjective(expr, GRB.MINIMIZE);


			// Integrate new variables

			master.update();


			// Add visit constraint
			GRBConstr[] cons_visit = new GRBConstr[nNode];
			for(j = 1; j < nNode; j++){
				expr = new GRBLinExpr();
				for(int d = 0 ;d < da.nDepot;d++){
					for(int p = 0; p < routes.get(d).size();p++){
						if(routes.get(d).get(p).contains(j))
							expr.addTerm(1, lambda[d].getElement(p));
					}
				}

				cons_visit[j] = master.addConstr(expr, GRB.GREATER_EQUAL, 1, "c1"+j);
			}
			this.iptime= System.currentTimeMillis();
			master.optimize();
			this.iptime = System.currentTimeMillis()-iptime;
			// print master status
			double objval = Double.MAX_VALUE;

			int optimstatus = master.get(GRB.IntAttr.Status);
			if (optimstatus == GRB.Status.OPTIMAL) {
				objval = master.get(DoubleAttr.ObjVal);
				System.out.println("Current Optimal objective: " + objval);
			} else{
				System.out.println("CG: relaxation infeasible!");
				return 1E10;
			}





			upperbound = master.get(DoubleAttr.ObjVal);
			ArrayList<ArrayList<Route>> solutionRoute = new ArrayList<>();

			if (master.get(GRB.IntAttr.SolCount) > 0) {
				for(i = 0; i < da.nDepot; i++) {
					solutionRoute.add(new ArrayList<>());
					for (int p = 0; p < routes.get(i).size(); p++) {
						if (lambda[i].getElement(p).get(DoubleAttr.X) > 0.5) {
							solutionRoute.get(i).add(routes.get(i).get(p));
						}
					}
				}
			}

			String filename = "IPSolutionRoute\\random_color"+da.nNode+"-"+da.nDepot;
			Functions.saveRoutes(filename, solutionRoute);

			// Dispose of model and environment
			master.dispose();
			env.dispose();
			return objval;

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
					e.getMessage());
			e.printStackTrace();
		}

		return 1E10;
	}



	public class GRBVarArray {
		// Creation of a new class similar to an ArrayList for Gurobi unknowns
		int _num = 0;
		GRBVar[] _array = new GRBVar[32];

		void add(GRBVar ivar) {
			if (_num >= _array.length) {
				GRBVar[] array = new GRBVar[2 * _array.length];
				System.arraycopy(_array, 0, array, 0, _num);
				_array = array;
			}
			_array[_num++] = ivar;
		}

		GRBVar getElement(int i) {
			return _array[i];
		}

		int getSize() {
			return _num;
		}
	}

	public ArrayList<Route> savings(ArrayList<Route> preRoutes, int depot) {
		ArrayList<Route> result = new ArrayList<>();
		boolean flag = true;
		while (flag){
			flag = false;
			if((preRoutes.size()%100) == 0){
				System.out.println(preRoutes.size());
			}
			for(int i = 0;i < preRoutes.size(); i++){
				if(preRoutes.get(i).removed == true)
					continue;
				for(int j = 0; j < preRoutes.size(); j++){
					if(i == j)
						continue;
					if(preRoutes.get(j).removed == true)
						continue;
					Route r1 = preRoutes.get(i);
					Route r2 = preRoutes.get(j);
					int u = r1.route.get(r1.route.size()-1);
					int v = r2.route.get(0);

					if(da.distance[depot][u][0] + da.distance[depot][0][v] > da.distance[depot][u][v]){
						ArrayList<Integer> newRoute = new ArrayList<>();
						newRoute.addAll(r1.route);
						newRoute.addAll(r2.route);
						if(checkFeasi(newRoute, depot)){
							//System.out.println(r1.route.toString() + " "+ r2.route.toString());
							preRoutes.get(i).removed = true;
							preRoutes.get(j).removed = true;
							preRoutes.add(new Route(newRoute));
							//System.out.println(newRoute.toString());
							flag = true;
							break;
						}
					}
				}
			}

		}
		for(int i = 0; i < preRoutes.size(); i++){
			if(preRoutes.get(i).removed == false){
				result.add(preRoutes.get(i));
			}
		}
		return result;
	}

	public boolean checkFeasi(ArrayList<Integer> route, int depot){
		boolean result = false;
		if(route.size() == 0)
			return false;
		double time = da.tw_a[0] + da.distance[depot][0][route.get(0)];
		int cap = da.demand[route.get(0)+da.dDepot];
		for(int i = 0; i < route.size() - 1; i++){
			if(time < da.tw_a[route.get(i)+da.dDepot]){
				time = da.tw_a[route.get(i)+da.dDepot];
			}else if(time > da.tw_b[route.get(i)+da.dDepot]){
				return false;
			}else{
				time = time + da.distance[depot][route.get(i)][route.get(i+1)];
			}
			cap += da.demand[route.get(i+1)+da.dDepot];
			if(cap > da.capacity){
				return false;
			}
		}
		if(time < da.tw_a[route.get(route.size() - 1)+da.dDepot]){
			time = da.tw_a[route.get(route.size() - 1)+da.dDepot];
		}else if(time > da.tw_b[route.get(route.size() - 1)+da.dDepot]){
			return false;
		}else{
			time = time + da.distance[depot][route.get(route.size() - 1)][0];
		}

		if(time < da.tw_b[0]){
			return true;
		}


		return result;

	}

}

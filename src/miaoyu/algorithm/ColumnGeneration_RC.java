/**
 * This class contains the column generation solver to BCP for VRPCC.
 * @author Miao Yu
 * @since May 6, 2017
 *
 */
package miaoyu.algorithm;

import miaoyu.helper.Data;
import miaoyu.helper.Route;
import gurobi.*;
import gurobi.GRB.DoubleAttr;
import pulse.DataHandler;
import pulse.GraphManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ColumnGeneration_RC {
	public Data data;
	public boolean isStrongBranching;
	public double lowerbound;
	public boolean initialized;
	public ArrayList<Route> routes;
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

	public ColumnGeneration_RC(Data d){
		this.data = d;
		this.nNode = d.nNode;
		this.nVehicle = d.nVehicle;
		this.initialized = false;
		this.solRoutes = new ArrayList<>();
		this.routes = new ArrayList<>();
		this.nIter = 0;
		this.nColumns = 0;
		this.timer = System.currentTimeMillis();
	}

	/**
	 * Depreciated
	 *
	 * @return
	 */
	public double solve()throws GRBException, InterruptedException, NumberFormatException, IOException {
		int i, j, k;
		try {

			String logName = "log\\RC\\"+data.fileName;
			File f = new File(logName);
			f.delete();
			GRBEnv   env   = new GRBEnv(logName);
			GRBModel master = new GRBModel(env);


			//double cost;
			//update cost of routes
			for (Route r : routes) {
				r.updateCost(data.distance);
			}

			// complete the lp with basic route to ensure feasibility
			int tot_size = 0;
			tot_size = routes.size();


			if (tot_size < 1) { //execute only the first time
				System.out.println("Initialize!");
					for(i = 1; i < nNode; i++){
						ArrayList<Integer> temp = new ArrayList<Integer>(1);
						temp.add(i);
						Route tempRoute = new Route(temp);
						tempRoute.updateCost(data.distance);
						routes.add(tempRoute);
					}
			}

			routes = savings(routes);
			for(Route r : routes){
				r.updateCost(data.distance);
			}
			// create variables
			GRBVarArray lambda = new GRBVarArray();
			for(int p = 0; p < routes.size(); p++){
				lambda.add(master.addVar(0, 1, 0, GRB.CONTINUOUS, "lambda"+p));
			}

			// Add constraints : (2.7)
			GRBLinExpr expr = new GRBLinExpr();

				expr = new GRBLinExpr();
				for(int p = 0; p < routes.size();p++){
					expr.addTerm(routes.get(p).cost, lambda.getElement(p));
					//System.out.println("v"+k+": "+routes[k].get(p).getCost());
				}
			master.setObjective(expr, GRB.MINIMIZE);


			// Integrate new variables

			master.update();


			// Add visit constraint
			GRBConstr[] cons_visit = new GRBConstr[nNode];
			for(j = 1; j < nNode; j++){
				expr = new GRBLinExpr();
						for(int p = 0; p < routes.size();p++){
							if(routes.get(p).contains(j))
								expr.addTerm(1, lambda.getElement(p));
						}
				cons_visit[j] = master.addConstr(expr, GRB.GREATER_EQUAL, 1, "c1"+j);
			}




			boolean oncemore = true;
			double objval = Double.MAX_VALUE;
			timer = System.currentTimeMillis();
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

				if(System.currentTimeMillis() - timer > 900000) {
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
					//					System.out.println("printing the updated cost matrix for vehicle "+k);
					//					for(i = 0; i < nVehicle; i++){
					//						System.out.println(Arrays.toString(data.cost[i]));
					//					}

					Set<Route>newroute = new HashSet<Route>();
					//ESPPRC pp = new ESPPRC(data);
					ESPPRC_RC pprc = new ESPPRC_RC(data);
					data.setThread(1);
					//RC_MultiThread pprc = new RC_MultiThread(data);

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
							lambda.add(newvar);
							routes.add(r);
							nColumns++;
						}
						timerBeforeLast = System.currentTimeMillis() - timer;
					//}
//				} else if(pp.solve(5)) {
//						oncemore = true;
//						newroute = pp.solutionSet;
//						System.out.println("number of new route found by ESPPRC:" + newroute.size());
//						for (Route r : newroute) {
//							GRBColumn col = new GRBColumn();
//							for (int v : r.route) {
//								System.out.print(v + ",");
//								col.addTerm(1, cons_visit[v]);
//							}
//							r.updateCost(data.distance);
//							//							System.out.println("Path for "+k+": "+Arrays.toString(r.route.toArray())+" "+r.cost);
//							//							System.out.println(col);
//							GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col, "lambda");
//							lambda.add(newvar);
//							routes.add(r);
//							nColumns++;
//						}
					//}
					}else{
						timerBeforeLast = System.currentTimeMillis() - timer;

						oncemore = false;

						//master.optimize();
						//ESPPRC pp = new ESPPRC(data);
						String dataFile = null;
						String dir = "Solomon Instances/";
						int here;
						if(data.fileName.substring(data.fileName.length()-5).startsWith("\\")||data.fileName.substring(data.fileName.length()-5).startsWith("/")){
							here = 4;
						}else{
							here = 5;
						}
						String fo = data.fileName.substring(data.fileName.length()-here);
						System.out.println(fo);
						String instanceType = fo.substring(0,here-3);
						int instanceNumber = Integer.parseInt(fo.substring(here-3,here));
						String extension = ".txt";
						dataFile = dir + instanceType + instanceNumber + extension;
						System.out.println("Instance: "+dataFile);

						// Read data file and define the following parameters: number of threads, number of nodes, and step size for the bounding procedure
						int numThreads = 1;
						int numNodes = data.nNode-1;
						int stepSize = 1;
						DataHandler d= new DataHandler(dataFile, instanceType, instanceNumber, numThreads, stepSize);
						d.readSolomon(numNodes, data.capacity);
						// Generate an ESPPRC instance with dual variables taken from an iteration of the CG (only available for the R-200 series!)
						d.generateInstance(alpha);
						//System.out.println("done load");
////////////////////////////////////////////////// BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////
						long tNow = System.currentTimeMillis(); 							// Measure current execution time

						GraphManager.calNaiveDualBound();									// Calculate a naive lower bound
						GraphManager.capIncumbent=data.capacity;				// Capture the depot upper time window
						int lowerCapLimit = 2; 											// Lower time (resource) limit to stop the bounding procedure. For 100-series we used 50 and for 200-series we used 100;
						int capIndex=0;													// Index to store the bounds
						//System.out.println("initialize");
						while(GraphManager.capIncumbent>=lowerCapLimit){					// Check the termination condition

							capIndex=(int) Math.ceil((GraphManager.capIncumbent/DataHandler.boundStep));		// Calculate the current index
							for (int x = 1; x <= DataHandler.n; x++) {
								//GraphManager.nodes[x].pulseBound(0, GraphManager.timeIncumbent, 0 , new ArrayList(), x,0); 	// Solve an ESPPRC for all nodes given the time incumbent
								GraphManager.nodes[x].pulseBound(GraphManager.capIncumbent, 0, 0 , new ArrayList(), x,0); 	// Solve an ESPPRC for all nodes given the time incumbent

							}

							for(int x=1; x<=DataHandler.n; x++){
								GraphManager.boundsMatrix[x][capIndex]=GraphManager.bestCost[x];				// Store the best cost found for each node into the bounds matrix
							}
							GraphManager.overallBestCost=GraphManager.PrimalBound;					// Store the best cost found over all the nodes
							GraphManager.capIncumbent-=DataHandler.boundStep;						// Update the time incumbent
						}

						System.out.println("here!");

////////////////////////////////////////////////END OF BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////

						// Run pulse
						GraphManager.capIncumbent+=DataHandler.boundStep; 				// Set time incumbent to the last value solved
						GraphManager.PrimalBound=0;										// Reset the primal bound

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
							ArrayList<Integer> rls = new ArrayList<>();
							for(i = 1; i < GraphManager.finalNode.Path.size() - 1; i++){
								rls.add((Integer) GraphManager.finalNode.Path.get(i));
							}
							Route r = new Route(rls);
							System.out.println("number of new route found:" + newroute.size());
							GRBColumn col = new GRBColumn();
							for(int v : r.route){
								System.out.print(v + ",");
								col.addTerm(1, cons_visit[v]);
							}
							r.updateCost(data.distance);
							//							System.out.println("Path for "+k+": "+Arrays.toString(r.route.toArray())+" "+r.cost);
							//							System.out.println(col);
							GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col,"lambda");
							lambda.add(newvar);
							routes.add(r);
							nColumns += 1;

						}
						master.update();
					}

					//pp = null;
					pprc = null;
				master.update();
			}

			lowerbound = master.get(DoubleAttr.ObjVal);

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

	public ArrayList<Route> savings(ArrayList<Route> preRoutes) {
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

					if(data.distance[u][0] + data.distance[0][v] > data.distance[u][v]){
						ArrayList<Integer> newRoute = new ArrayList<>();
						newRoute.addAll(r1.route);
						newRoute.addAll(r2.route);
						if(checkFeasi(newRoute)){
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

	public boolean checkFeasi(ArrayList<Integer> route){
		boolean result = false;
		if(route.size() == 0)
			return false;
		double time = data.tw_a[0] + data.distance[0][route.get(0)];
		int cap = data.demand[route.get(0)];
		for(int i = 0; i < route.size() - 1; i++){
			if(time < data.tw_a[route.get(i)]){
				time = data.tw_a[route.get(i)];
			}else if(time > data.tw_b[route.get(i)]){
				return false;
			}else{
				time = time + data.distance[route.get(i)][route.get(i+1)];
			}
			cap += data.demand[route.get(i+1)];
			if(cap > data.capacity){
				return false;
			}
		}
		if(time < data.tw_a[route.get(route.size() - 1)]){
			time = data.tw_a[route.get(route.size() - 1)];
		}else if(time > data.tw_b[route.get(route.size() - 1)]){
			return false;
		}else{
			time = time + data.distance[route.get(route.size() - 1)][0];
		}

		if(time < data.tw_b[0]){
			return true;
		}


		return result;

	}

}

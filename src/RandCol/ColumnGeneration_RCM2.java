/**
 * This class contains the column generation solver to BCP for VRPCC.
 * @author Miao Yu
 * @since May 6, 2017
 *
 */
package RandCol;

import gurobi.*;
import gurobi.GRB.DoubleAttr;
import pulse.DataHandler;
import pulse.GraphManager;

import java.io.*;
import java.util.*;

public class ColumnGeneration_RCM2 {
	public Data data;
	public boolean isStrongBranching;
	public double lowerbound;
	public double upperbound;
	public boolean initialized;
	public List<Route> routes;
	public Set<Route> routeSet;
	public List<Route> artificialRoute;
	public List<Route> solRoutes;
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
	public long etime;
	public int count;
	public List<Cut> cutPool;
	public List<Integer>[][] edgeToCut;
	public List<Integer>[][] edgeToRoute;
	public boolean duplicateRoute = false;
	public double getUpperbound(){
		ArrayList<String[]> rawData = new ArrayList<String[]>();
		String csvFile = "Data\\UCVRP"+data.capacity+"\\RandCol-multi\\"+data.fileName+"-"+data.nNode+"-ii.csv";
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


	public void convertRoute2Edge(Route r, int idx){
		int prev = 0;
		for(int cur : r.route){
			edgeToRoute[prev][cur].add(idx);
			prev = cur;
		}
		if(prev != 0) edgeToRoute[prev][0].add(idx);
	}

	public ColumnGeneration_RCM2(Data d){
		this.data = d;
		this.nNode = d.nNode;
		this.nVehicle = d.nVehicle;
		this.initialized = false;
		this.solRoutes = new ArrayList<>();
		this.routes = new ArrayList<>();
		this.nIter = 0;
		this.nColumns = 0;
		this.timer = System.currentTimeMillis();
		this.cutPool = new ArrayList<>();
		this.edgeToRoute = new List[nNode][nNode];
		this.edgeToCut = new List[nNode][nNode];
		for(int i = 0; i < nNode; i++)
			for(int j = 0; j < nNode; j++){
				edgeToRoute[i][j] = new ArrayList<>();
				edgeToCut[i][j] = new ArrayList<>();
			}
		this.routeSet = new HashSet<>();
	}


	public double solve()throws GRBException, InterruptedException, NumberFormatException, IOException {
		int i, j, k;
		try {

			String logName = "log/RC_"+data.fileName;
			File f = new File(logName);
			f.delete();
			GRBEnv   env   = new GRBEnv(logName);
			GRBModel master = new GRBModel(env);
			master.set(GRB.IntParam.Threads, 8);


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

			System.out.println("original routes: "+routes.size());

			routes = savings(routes);

			for(int p = 0; p < routes.size(); p++){
				routes.get(p).updateCost(data.distance);
				convertRoute2Edge(routes.get(p), p);
			}
			for( k = 0; k < nNode; k++){
				System.out.println(k+":"+edgeToRoute[0][k]);
			}
			System.out.println("initial routes: "+routes.size());
			routeSet.addAll(routes);
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

			// initialize cut contraints list
			List<GRBConstr> cut_enforce = new ArrayList<>();

			master.update();
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

				if(System.currentTimeMillis() - timer > 900000) {
					lowerbound = objval;
					return objval;
				}
				// primal solution
				double[] xroute = new double[lambda.getSize()];
				for(i = 0; i < lambda.getSize(); i++){
					xroute[i] = lambda.getElement(i).get(DoubleAttr.X);
				}




				// dual solution for visited node
				double[] alpha = new double[nNode];
				//double[] gamma = new double[K];
				for(i = 1; i < nNode; i++){
					alpha[i] = cons_visit[i].get(DoubleAttr.Pi);
				}
				// dual solution for cuts
				double[] beta = new double[cut_enforce.size()];
				for(i = 0; i < cut_enforce.size(); i++){
					beta[i] = cut_enforce.get(i).get(DoubleAttr.Pi);
				}

				System.out.println("print dual solutions");
				System.out.println("beta");
				System.out.println(Arrays.toString(beta));


				// update cost for each pricing problem
				System.out.println("Solving pricing problem");
//				for (i = 1; i < nNode; i++){
//					if(data.distance[0][i] > 10000 ){
//						data.cost[0][i] = data.distance[0][i];
//					}else{
//						data.cost[0][i] = data.distance[0][i] - alpha[i]/2;
//					}
//				}
				for (j = 0; j < nNode; j++){
					if(data.distance[j][0] > 100000){
						data.cost[j][nNode] = data.distance[j][0];
					}else{
						data.cost[j][nNode] = data.distance[j][0]- alpha[j]/2;
					}
					//data.cost[u][0] = data.vDistanceMatrix.get(k)[u][0];
					for (i = 0; i < nNode; i++){
						if(data.distance[j][i] > 100000 ){
							data.cost[j][i] = data.distance[j][i];
						}else{
							data.cost[j][i] = data.distance[j][i] - alpha[i]/2- alpha[j]/2;
						}
					}
				}

				// update reduced cost of cuts
				for(int l = 0; l < cut_enforce.size(); l++) {
					Cut c = cutPool.get(l);
					for(int[] e : c.getEdges()){
						int from = e[0];
						int to = e[1];
						if(to == 0) data.cost[from][nNode] -= beta[l];
						else data.cost[from][to] -= beta[l];

					}
				}

				// generate cuts
				List<Route> sol_routes = new ArrayList<>();
				for(i = 0; i < lambda.getSize(); i++){
					if(xroute[i] > 0){
						sol_routes.add(routes.get(i));
						routes.get(i).sol = xroute[i];
					}
				}
				Seperator2 spt = new Seperator2(nNode, sol_routes, data.capacity);
				spt.generate();
				if(spt.getCutPool().size() != 0) oncemore = true;
				for(Cut c : spt.getCutPool()){

					this.cutPool.add(c);

					expr = new GRBLinExpr();
					for(int[] edge: c.getEdges()){
						for(Integer idx : edgeToRoute[edge[0]][edge[1]]){
								expr.addTerm(1, lambda.getElement(idx));
						}
						edgeToCut[edge[0]][edge[1]].add(cut_enforce.size());
					}
					cut_enforce.add(master.addConstr(expr, GRB.GREATER_EQUAL,c.getRhs(), "cut"));
//					for( k = 0; k < nNode; k++){
//						System.out.println(k+":"+edgeToRoute[0][k]);
//					}
//					System.out.println(routes.get(4).route);
//					return 0;
				}



				Set<Route>newroute = new HashSet<Route>();
				data.setThread(8);
				//RCm_MT pprc = new RCm_MT(data);
				//RC_MultiThread pprc = new RC_MultiThread(data);
				RandColW pprc = new RandColW(data);

				if(pprc.solve()) {
					oncemore = true;
					newroute = pprc.solutionSet;
					System.out.println("number of new route found:" + newroute.size());
					int oldnumber = routes.size();
					for (Route r : newroute) {
						GRBColumn col = new GRBColumn();
						for (int v : r.route) {
							col.addTerm(1, cons_visit[v]);
						}
						r.updateCost(data.distance);
						nColumns++;
						int prev = 0;
						Map<Integer, Integer> cutCounter = new HashMap<>();
						for(int cur : r.route){
							List<Integer> lst = edgeToCut[prev][cur];
							for(int c : lst){
								//col.addTerm(1, cut_enforce.get(c));
								cutCounter.put(c, cutCounter.getOrDefault(c, 0)+1);
							}
							prev = cur;
						}
						if(prev != 0){
							List<Integer> lst = edgeToCut[prev][0];

							for(int c : lst){
								cutCounter.put(c, cutCounter.getOrDefault(c, 0)+1);

								//col.addTerm(1, cut_enforce.get(c));
							}
						}
						for(Integer c : cutCounter.keySet())
							col.addTerm(cutCounter.get(c), cut_enforce.get(c));
						GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col, "lambda");
						lambda.add(newvar);

						convertRoute2Edge(r, routes.size());

						routes.add(r);
						if(routeSet.contains(r)) duplicateRoute = true;
						else routeSet.add(r);
					}
					master.update();
					timerBeforeLast = System.currentTimeMillis() - timer;
					//}
					if(duplicateRoute) System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");

				}
				// applying pulse to verify no more paths can be generated
				else {
					oncemore = false;
					String dataFile = null;
					String dir = "Instances/";
					System.out.println("start pulse");
					int here;
					System.out.println(data.fileName);

					String fo = data.fileName;
					System.out.println(fo);
					String instanceType = fo.substring(0,fo.length()-3);
					int instanceNumber = Integer.parseInt(fo.substring(fo.length()-3));
					String extension = ".txt";
					dataFile = dir + instanceType + instanceNumber + extension;
					System.out.println("Instance: "+dataFile);
					//System.out.println("done load");

					// Read data file and define the following parameters: number of threads, number of nodes, and step size for the bounding procedure
					int numThreads = 1;
					int numNodes = data.nNode-1;
					int stepSize = 1;
					DataHandler d = new DataHandler(dataFile, instanceType, instanceNumber, numThreads, stepSize);
					System.out.println("!"+numNodes);
					d.readSolomon(numNodes, data.capacity);
					// Generate an ESPPRC instance with dual variables taken from an iteration of the CG (only available for the R-200 series!)
					d.generateInstance(alpha);
					System.out.println("done load");
////////////////////////////////////////////////// BOUNDING PROCEDURE //////////////////////////////////////////////////////////////////////////
					long tNow = System.currentTimeMillis(); 							// Measure current execution time

					GraphManager.calNaiveDualBound();									// Calculate a naive lower bound
					GraphManager.capIncumbent=d.Q;				// Capture the depot upper time window
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
								col.addTerm(1, cons_visit[v]);
							}
							int prev = 0;
							Map<Integer, Integer> cutCounter = new HashMap<>();
							for(int cur : r.route){
								List<Integer> lst = edgeToCut[prev][cur];
								for(int c : lst){
									//col.addTerm(1, cut_enforce.get(c));
									cutCounter.put(c, cutCounter.getOrDefault(c, 0)+1);
								}
								prev = cur;
							}
							if(prev != 0){
								List<Integer> lst = edgeToCut[prev][0];

								for(int c : lst){
									cutCounter.put(c, cutCounter.getOrDefault(c, 0)+1);

									//col.addTerm(1, cut_enforce.get(c));
								}
							}
							for(Integer c : cutCounter.keySet())
								col.addTerm(cutCounter.get(c), cut_enforce.get(c));
							r.updateCost(data.distance);

							GRBVar newvar = master.addVar(0, 1, r.cost, GRB.CONTINUOUS, col,"lambda");
							lambda.add(newvar);
							routes.add(r);
							nColumns += 1;
							convertRoute2Edge(r, routes.size()-1);
							master.update();
						}

					}


					master.update();
				}



				pprc = null;
				master.update();
			}
			lptime = System.currentTimeMillis() - lptime;
			lowerbound = master.get(DoubleAttr.ObjVal);

			/**
			 * Enumeration!
			 */

			//***************************
			//*****Enumeration!
			//****************************
			double[] alpha = new double[nNode];
			//double[] gamma = new double[K];
			for(i = 1; i < nNode; i++){
				alpha[i] = cons_visit[i].get(DoubleAttr.Pi);
			}

			// dual solution for cuts
			double[] beta = new double[cut_enforce.size()];
			for(i = 1; i < cut_enforce.size(); i++){
				beta[i] = cut_enforce.get(i).get(DoubleAttr.Pi);
			}

			System.out.println("print dual solutions");
			System.out.println("alpha");
			System.out.println(Arrays.toString(alpha));

			//				if(oncemore == false)
			//					break;
			// update cost for each pricing problem
			System.out.println("Solving pricing problem");
			for (i = 1; i < nNode; i++){
				if(data.distance[0][i] > 10000 ){
					data.cost[0][i] = data.distance[0][i];
				}else{
					data.cost[0][i] = data.distance[0][i] - alpha[i];
				}
			}
			for (j = 0; j < nNode; j++){
				if(data.distance[j][nNode] > 10000 ){
					data.cost[j][nNode] = data.distance[j][0];
				}else{
					data.cost[j][nNode] = data.distance[j][0];
				}
				//data.cost[u][0] = data.vDistanceMatrix.get(k)[u][0];
				for (i = 0; i < nNode; i++){
					if(data.distance[j][i] > 10000 ){
						data.cost[j][i] = data.distance[j][i];
					}else{
						data.cost[j][i] = data.distance[j][i] - alpha[i];
					}
				}
			}
			for(int l = 0; l < cut_enforce.size(); l++) {
				Cut c = cutPool.get(l);
				for(int[] e : c.getEdges()){
					int from = e[0];
					int to = e[1];
					data.cost[from][to] -= beta[l];
					data.cost[to][from] -= beta[l];
					if(from == 0) data.cost[to][nNode] -= beta[l];
					if(to == 0) data.cost[from][nNode] -= beta[l];
				}
			}
			Set<Route>newroute = new HashSet<Route>();
			etime = System.currentTimeMillis();
			data.setThread(78);
			RandColEnum pprc = new RandColEnum(data);
			//pprc.setEpsilon(getUpperbound(), lowerbound);
			count = routes.size();
//			if(pprc.solve()) {
//				newroute = pprc.solutionSet;
//				System.out.println("number of new route found:" + newroute.size());
//				for (Route r : newroute) {
//					r.updateCost(data.distance);
//					routes.add(r);
//				}
//			}
			count = routes.size()-count;
			etime = System.currentTimeMillis()-etime;


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

	public double solveMIP()throws InterruptedException, NumberFormatException, IOException {
		int i, j, k;
		try {
			String logName = "log\\ES\\"+data.fileName;
			File f = new File(logName);
			f.delete();
			GRBEnv   env   = new GRBEnv(logName);
			GRBModel master = new GRBModel(env);
			master.set(GRB.IntParam.Threads, 39);
			master.set(GRB.DoubleParam.TimeLimit, 1800);

			//double cost;
			//update cost of routes
			for (Route r : routes) {
				r.updateCost(data.distance);
			}

			// complete the lp with basic route to ensure feasibility
			int tot_size = 0;
			tot_size = routes.size();


			for(Route r : routes){
				r.updateCost(data.distance);
			}
			// create variables
			GRBVarArray lambda = new GRBVarArray();
			for(int p = 0; p < routes.size(); p++){
				lambda.add(master.addVar(0, 1, 0, GRB.BINARY, "lambda"+p));
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

			iptime = System.currentTimeMillis();
			master.optimize();
			iptime = System.currentTimeMillis() - iptime;
			// print master status
			double objval = Double.MAX_VALUE;

			int optimstatus = master.get(GRB.IntAttr.Status);
			if (optimstatus == GRB.Status.OPTIMAL) {
				objval = master.get(DoubleAttr.ObjVal);
				System.out.println("Current Optimal objective: " + objval);
			}



			upperbound = master.get(DoubleAttr.ObjVal);


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

	public ArrayList<Route> savings(List<Route> preRoutes) {
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

	public boolean checkFeasi(List<Integer> route){
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

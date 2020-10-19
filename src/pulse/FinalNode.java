package pulse;

import java.util.ArrayList;

/** This class represents the final node. It holds the pulse method override for the final node.

 * Ref.: Lozano, L., Duque, D., and Medaglia, A. L. (2013). 

 * An exact algorithm for the elementary shortest path problem with resource constraints. Technical report COPA 2013-2  

 * @author L. Lozano & D. Duque

 * @affiliation Universidad de los Andes - Centro para la Optimizacin y Probabilidad Aplicada (COPA)

 * @url http://copa.uniandes.edu.co/ 
 * 
 * */

public class FinalNode extends Node {
	
	
	
	public int id;			// Node id
	public int demand; 		// Node demand
	public int service;		// Node service Time
	public int tw_a;		// Beginning of the time window
	public int tw_b;		// End of the time window
	public ArrayList<Integer> magicIndex; // Array with the indexes of all outgoing arcs from the node
	
	public ArrayList Path;	// Best solution found at any time of the exploration
	public double PathTime;	// Best solution time
	public double PathLoad;	// Best solution load
	public double PathCost;	// Best solution cost
	double PathDist;		// Best solution distance
	
	
	/** Class constructor
	 * @param i Node id
	 * @param d Node demand
	 * @param s Node service time
	 * @param a Lower time window
	 * @param b Upper time window
	 */
	public FinalNode(int i, int d, int s , int a, int b) {
		super(i, 0, 0, a, b);
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		magicIndex = new ArrayList<>();
		Path= new ArrayList();
	}
	

	
	
	/* Override for the bounding procedure
	 */
	public void pulseBound(double PLoad, double PTime, double PCost, ArrayList path, int Root, double PDist) {
		// If the path is feasible update values for the bounding matrix and primal bound
		if (PLoad <= DataHandler.Q && (PTime) <= tw_b) {

			if ((PCost) < GraphManager.bestCost[Root]) {
				GraphManager.bestCost[Root] = (PCost);

				if (PCost < GraphManager.PrimalBound) {
					GraphManager.PrimalBound = PCost;

				}
				
			}
		
		}

	}
	
	/* Override for the pulse procedure
	 */
	public synchronized void pulseMT(double PLoad, double PTime, double PCost, ArrayList path, double PDist, int thread) {
		// If the path is feasible and better than the best known solution update the best known solution and primal bound	
		if (PLoad <= DataHandler.Q && (PTime) <= tw_b) {
				if(PCost < -1E-6 && GraphManager.paths.size()< 30){
					ArrayList temp = new ArrayList<>();
					for (int i = 0; i < path.size(); i++) {
						temp.add(path.get(i));
					}
					temp.add(id);
					GraphManager.paths.add(temp);
				}
				if (PCost <= GraphManager.PrimalBound) {
					GraphManager.PrimalBound = PCost;
					this.PathTime = PTime;
					this.PathCost = PCost;
					this.PathLoad = PLoad;
					this.PathDist = PDist;
					this.Path.clear();
					for (int i = 0; i < path.size(); i++) {
						this.Path.add(path.get(i));
					}

					this.Path.add(id);
				}
		

			}

		}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Utilities, sorting and miscellaneous stuff!	
	
	public String toString(){
		
		return id+"";
	}
	
	public Object clone() {
		return super.clone();
	}
	
	private void SortF(ArrayList<Double> set) {
		QSF(set, 0, set.size() - 1);
	}

	public int putEndNode(ArrayList<Double> e, int b, int t) {
		int i;
		int pivot;
		double pivotVal;
		double temp;

		pivot = b;
		pivotVal = e.get(pivot) ;
		for (i = b + 1; i <= t; i++) {
			if (  e.get(i) < pivotVal) {
				pivot++;
				temp = e.get(i);
				e.set(i, e.get(pivot));
				e.set(pivot,temp);
			}
		}
		temp =  e.get(b);
		e.set(b, e.get(pivot));
		e.set(pivot,temp);
		return pivot;
	}

	public void QSF(ArrayList<Double> e, int b, int t) {
		int pivot;
		if (b < t) {
			pivot = putEndNode(e, b, t);
			QSF(e, b, pivot - 1);
			QSF(e, pivot + 1, t);
		}
	}


}


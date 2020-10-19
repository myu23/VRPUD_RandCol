
package pulse;

import java.util.ArrayList;

/** This class represents a node. It holds the pulse method and all the logic of the algorithm.

 * Ref.: Lozano, L., Duque, D., and Medaglia, A. L. (2013). 

 * An exact algorithm for the elementary shortest path problem with resource constraints. Technical report COPA 2013-2  

 * @author L. Lozano & D. Duque

 * @affiliation Universidad de los Andes - Centro para la Optimizacin y Probabilidad Aplicada (COPA)

 * @url http://copa.uniandes.edu.co/
 *
 */

public class Node {
	
	
	
	public int id;			// Node number
	public int demand; 		// Node demand
	public int service;		// Node service time
	public int tw_a;		// Beginning of the time window
	public int tw_b;		// End of the time window
	public ArrayList<Integer> magicIndex;	// Array with the indexes of all outgoing arcs from the node
	boolean firstTime=true;					// boolean that indicates if the node is visited for first time
		
	/** Class constructor
	 * @param i Node number
	 * @param d Node demand
	 * @param s Node service time
	 * @param a Lower time window
	 * @param b Upper time window
	 */
	public Node(int i, int d, int s , int a, int b) {
		id = i;
		demand = d;
		service = s;
		tw_a = a;
		tw_b = b;	
		magicIndex = new ArrayList<>();
	}
	
	
	
	/**
	 * Pulse function for the bounding stage
	 * @param pLoad current path load
	 * @param pTime current path time
	 * @param pCost current path cost
	 * @param path current path
	 * @param root current root node
	 * @param pDist current path distance
	 */
	public void pulseBound(double pLoad, double pTime, double pCost, ArrayList path, int root, double pDist) {
		
		// If the node is visited for the first time, sort the outgoing arcs array 
		if(this.firstTime==true){
			this.firstTime=false;
			this.Sort(this.magicIndex);
		}
		
		// If the node is reached before the lower time window wait until the beginning of the time window
		if(pTime<this.tw_a){
			pTime=this.tw_a;
		}		
		
		// Try to prune pulses with the pruning strategies: cycles, infeasibility, bounds, and rollback
		if(GraphManager.visited[id]==0 && pLoad <= DataHandler.Q &&pTime <= tw_b && (pCost+calcBoundPhaseI(pLoad,root))<GraphManager.bestCost[root] && !rollback(path,pCost,pTime)){
			// If the pulse is not pruned add it to the path
			GraphManager.visited[id] = 1;
			path.add(id);
			// Propagate the pulse through all the outgoing arcs
			for (int i = 0; i < magicIndex.size(); i++) {

				double newPLoad = 0;
				double newPTime = 0;
				double newPCost = 0;
				double newPDist = 0;
				int arcHead = DataHandler.arcs[magicIndex.get(i)][1];
				
				// Update all path attributes
				newPTime = (pTime+DataHandler.timeList[magicIndex.get(i)]);
				newPCost = (pCost + DataHandler.costList[magicIndex.get(i)]);
				newPLoad = (pLoad + DataHandler.loadList[magicIndex.get(i)]);
				newPDist = (pDist + DataHandler.distList[magicIndex.get(i)]);
				
				// Check feasibility and propagate pulse
				if (newPTime <= GraphManager.nodes[arcHead].tw_b
					&& newPLoad <= DataHandler.Q && newPTime <= GraphManager.nodes[0].tw_b) {
				// If the head of the arc is the final node, pulse the final node	
					if(arcHead==0){
						GraphManager.finalNode.pulseBound(newPLoad, newPTime, newPCost, path, root,newPDist);	
					}
					else{
						GraphManager.nodes[arcHead].pulseBound(newPLoad, newPTime, newPCost, path, root ,newPDist);
					}
				}

			}
			// Remove the explored node from the path
			path.remove((path.size() - 1));
			GraphManager.visited[id] = 0;

		}
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7
public void pulseMTBound(double PLoad, double PTime, double PCost, ArrayList path, int root, double PDist, int thread) throws InterruptedException {

	// If the node is visited for the first time, sort the outgoing arcs array
	if(this.firstTime==true){
		this.firstTime=false;
		this.Sort(this.magicIndex);
	}

	// If the node is reached before the lower time window wait until the beginning of the time window
	if(PTime<this.tw_a){
		PTime=this.tw_a;
	}

	//statistics
	if (GraphManager.visitedMT[id][thread]==0 && (PCost+CalcBoundPhaseII(PLoad))<GraphManager.PrimalBound){
		GraphManager.count_bound++;
		GraphManager.count_depth_bound += path.size();
	}

	// Try to prune pulses with the pruning strategies
	if((GraphManager.visitedMT[id][thread]==0 && PLoad <= DataHandler.Q &&PTime <= tw_b && (PCost+calcBoundPhaseI(PLoad,root))<GraphManager.bestCost[root] && !rollback(path,PCost,PTime))){
		// If the pulse is not pruned add it to the path
		GraphManager.visitedMT[id][thread]=1;
		path.add(id);
		// Propagate the pulse through all the outgoing arcs
		for(int i=0; i<magicIndex.size(); i++){

			double NewPLoad = 0;
			double NewPTime = 0;
			double NewPCost = 0;
			double NewPDist = 0;
			int Head = DataHandler.arcs[magicIndex.get(i)][1];

			// Update all path attributes
			NewPTime=(PTime+DataHandler.timeList[magicIndex.get(i)]);
			NewPCost=(PCost+DataHandler.costList[magicIndex.get(i)]);
			NewPLoad=(PLoad+DataHandler.loadList[magicIndex.get(i)]);
			NewPDist=(PDist+DataHandler.distList[magicIndex.get(i)]);
			// Check feasibility and propagate pulse
			if( NewPTime<=GraphManager.nodes[Head].tw_b && NewPLoad<=DataHandler.Q && NewPTime<=GraphManager.nodes[0].tw_b){
				GraphManager.count_inf++;
				// If the head of the arc is the final node, pulse the final node
				if (Head == 0) {
					GraphManager.finalNode.pulseBound(NewPLoad,NewPTime,NewPCost, path, root, NewPDist);
				}else{
					// If not in the start node continue the exploration on the current thread
					if(id!=0){
						GraphManager.nodes[Head].pulseMTBound(NewPLoad,NewPTime,NewPCost, path, root, NewPDist, thread);
					}
					// If standing in the start node, wait for the next available thread to trigger the exploration
					else {
						boolean stopLooking = false;
						for (int j = 1; j < DataHandler.threads.length; j++) {
							if(!DataHandler.threads[j].isAlive()){
								DataHandler.threads[j] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, j));
								DataHandler.threads[j].start();
								stopLooking = true;
								j = 1000;
							}
						}
						if (!stopLooking) {
							DataHandler.threads[1].join();
							DataHandler.threads[1] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, 1));
							DataHandler.threads[1].start();
						}
					}
				}
			}

		}
		// Wait for all active threads to finish
		if(id==0){
			for (int k = 1; k < DataHandler.threads.length; k++) {
				DataHandler.threads[k].join();
			}
		}

		// Remove the explored node from the path
		path.remove((path.size()-1));
		GraphManager.visitedMT[id][thread]=0;
	}
}




	/** Multithread pulse function
 * @param PLoad current load
 * @param PTime current time
 * @param PCost current cost
 * @param path current partial path
 * @param PDist current distance
 * @param thread current thread 
 * @throws InterruptedException
 */
public void pulseMT(double PLoad, double PTime, double PCost, ArrayList path, double PDist, int thread) throws InterruptedException {

		// If the node is visited for the first time, sort the outgoing arcs array 
		if(this.firstTime==true){
			this.firstTime=false;
			this.Sort(this.magicIndex);
		}
		
		// If the node is reached before the lower time window wait until the beginning of the time window
		if(PTime<this.tw_a){
			PTime=this.tw_a;
		}

		//statistics
		if (GraphManager.visitedMT[id][thread]==0 && (PCost+CalcBoundPhaseII(PLoad))<GraphManager.PrimalBound){
			GraphManager.count_bound++;
			GraphManager.count_depth_bound += path.size();
		}

		// Try to prune pulses with the pruning strategies
		if((GraphManager.visitedMT[id][thread]==0 && (PCost+CalcBoundPhaseII(PLoad))<GraphManager.PrimalBound && !rollback(path,PCost,PTime))){
			// If the pulse is not pruned add it to the path
			GraphManager.visitedMT[id][thread]=1;
			path.add(id);	
			// Propagate the pulse through all the outgoing arcs
			for(int i=0; i<magicIndex.size(); i++){
				
				double NewPLoad = 0;
				double NewPTime = 0;
				double NewPCost = 0;
				double NewPDist = 0;
				int Head = DataHandler.arcs[magicIndex.get(i)][1];

				// Update all path attributes
				NewPTime=(PTime+DataHandler.timeList[magicIndex.get(i)]);
				NewPCost=(PCost+DataHandler.costList[magicIndex.get(i)]);
				NewPLoad=(PLoad+DataHandler.loadList[magicIndex.get(i)]);
				NewPDist=(PDist+DataHandler.distList[magicIndex.get(i)]);
				// Check feasibility and propagate pulse
				if( NewPTime<=GraphManager.nodes[Head].tw_b && NewPLoad<=DataHandler.Q && NewPTime<=GraphManager.nodes[0].tw_b){
					GraphManager.count_inf++;
					// If the head of the arc is the final node, pulse the final node
					if (Head == 0) {
						GraphManager.finalNode.pulseMT(NewPLoad,NewPTime,NewPCost, path, NewPDist,thread);
					}else{
						// If not in the start node continue the exploration on the current thread 
						if(id!=0){
							GraphManager.nodes[Head].pulseMT(NewPLoad,NewPTime,NewPCost, path, NewPDist, thread);	
						}
						// If standing in the start node, wait for the next available thread to trigger the exploration
						else {
							boolean stopLooking = false;
							for (int j = 1; j < DataHandler.threads.length; j++) {
								if(!DataHandler.threads[j].isAlive()){
									DataHandler.threads[j] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, j));
									DataHandler.threads[j].start();
									stopLooking = true;
									j = 1000;
								}
							}
							if (!stopLooking) {
								DataHandler.threads[1].join();
								DataHandler.threads[1] = new Thread(new PulseTask(Head, NewPLoad, NewPTime, NewPCost, path, NewPDist, 1));
								DataHandler.threads[1].start();
							}
						}
					}
				}
				
			}
		// Wait for all active threads to finish	
		if(id==0){
			for (int k = 1; k < DataHandler.threads.length; k++) {
				DataHandler.threads[k].join();
				}
			}						
	
		// Remove the explored node from the path
		path.remove((path.size()-1));
		GraphManager.visitedMT[id][thread]=0;
	}
}


/** Rollback pruning strategy
 * @param path current partial path
 * @param pCost current cost
 * @param pTime current time
 * @return
 */
private boolean rollback(ArrayList path, double pCost, double pTime) {
	// Can't use the strategy for the start node
	if(path.size()<=1){
		return false;
	}
	else{
		// Calculate the cost for the rollback pruning strategy 
		int prevNode = (int) path.get(path.size()-1);
		int directNode = (int) path.get(path.size()-2);
		double directCost = pCost-DataHandler.cost[prevNode][id]-DataHandler.cost[directNode][prevNode]+DataHandler.cost[directNode][id];
		double directTime = pTime-DataHandler.distance[prevNode][id]-DataHandler.cost[directNode][prevNode]+DataHandler.cost[directNode][id];
		
		
		if(directCost<=pCost ){
			GraphManager.count_roll++;
			return true;
		}
	}

	return false;
}


/** This method calculates a lower bound given a time consumption at a given node
* @param cap current time
* @param root current root node
* @return
*/
private double calcBoundPhaseI(double cap, int root) {

double Bound=0;
// If the time consumed is less than the last time incumbent solved and the node id is larger than the current root node being explored it means that there is no lower bound available and we must use the naive bound 
if(cap<GraphManager.capIncumbent+DataHandler.boundStep && this.id>=root){
	Bound=((GraphManager.capIncumbent+DataHandler.boundStep-cap)*GraphManager.naiveDualBound+GraphManager.overallBestCost);
}

else {
// Else use the available bound	
	int Index=((int) Math.floor(cap/DataHandler.boundStep));
	Bound=GraphManager.boundsMatrix[this.id][Index];
		
}

return Bound;
}


/** This method calculates a lower bound given a time consumption at a given node
* @param Cap current time
* @return
*/
private double CalcBoundPhaseII(double Cap) {


double Bound=0;
//If the time consumed is less than the current time incumbent it means that there is no lower bound available and we must use the naive bound 
if(Cap<GraphManager.capIncumbent){
	
	Bound=(GraphManager.capIncumbent-Cap)*GraphManager.naiveDualBound+GraphManager.overallBestCost;
	
}
else {
	// Else use the available bound	
	int Index=((int) Math.floor(Cap/DataHandler.boundStep));
			
	Bound=GraphManager.boundsMatrix[this.id][Index];
	
}


return Bound;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////7

// Utilities, sorting and miscellaneous stuff!

public int  getID()
{
	return id;
}

public String toString(){
	
	return id+"";
}

public Object clone() {
	try {
		return super.clone();
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
public void autoSort(){
	Sort(this.magicIndex);
}
private synchronized void Sort(ArrayList<Integer> set) {
	QS(set, 0, set.size() - 1);
}

public int put(ArrayList<Integer> e, int b, int t) {
	int i;
	int pivot;
	double pivotVal;
	int temp;

	pivot = b;
	pivotVal = DataHandler.costList[e.get(pivot)] ;
	for (i = b + 1; i <= t; i++) {
		if (   DataHandler.costList[e.get(i)]< pivotVal) {
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

public void QS(ArrayList<Integer> e, int b, int t) {
	int pivot;
	if (b < t) {
		pivot = put(e, b, t);
		QS(e, b, pivot - 1);
		QS(e, pivot + 1, t);
	}
}


}

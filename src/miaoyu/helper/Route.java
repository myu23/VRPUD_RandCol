package miaoyu.helper;
import java.util.*;

/**
 * Route Class for a route in vehicle routing problem
 * 
 * @author Miao Yu
 * @version 2.0
 * @since Sep 6, 2020
 */

public class Route {
	public ArrayList<Integer> route; //The route list only contains customer nodes
	public double cost; //cost of the route
	public double reduceCost; //reduced cost of the route
	public double sol;
	public int vehicleIndex;
	public boolean inPool;
	public boolean removed = false;
	public int lastActive = -1;

	/**
	 * Constructor
	 * Create an instance from a list of customer nodes.
	 * @param lst a liist of customer nodes
	 * @return
	 */
	public Route(ArrayList<Integer> lst){
		route = new ArrayList<Integer>(8);
		for(int customer : lst){
			route.add(customer);
		}
		inPool = false;
		lastActive = -1;
	}

	/**
	 * Default constructor
	 */
	public Route(){
		route = new ArrayList<>();
	}

	/**
	 * Update the cost of the route given a distance matrix
	 * Apply only to VRP with single depot located at index 0.
	 * @param dist input distance matrix
	 * @return
	 */
	public void updateCost(double[][] dist){
		int current = 0;
		this.cost = 0;
		int i, next;
		for(i = 0; i < route.size(); i++){
			next = route.get(i);
			cost += dist[current][next];
			current = next;
		}
		cost += dist[current][0];
	}
	/**
	 * Update the cost of the route given a distance matrix
	 * Apply to multi-depot VRP and the route is rooted in index root.
	 *
	 * @param dist input distance matrix
	 * @param root depot index of the route
	 * @return
	 */
	public void updateCost(int root, double[][] dist){
		int current = root;
		this.cost = 0;
		int i, next;
		for(i = 0; i < route.size(); i++){
			next = route.get(i);
			cost += dist[current][next];
			current = next;
		}
		cost += dist[current][root];
	}

	/**
	 * update reduceCost of the route
	 * @param rdist reduced cost distance matrix
	 * @return
	 */
	public void updateReduceCost(double[][] rdist){
		int current = 0;
		int i, next;
		for(i = 0; i < route.size(); i++){
			next = route.get(i);
			reduceCost += rdist[current][next];
			current = next;
		}
		reduceCost += rdist[current][0];
	}


	/**
	 * check if route contains edge (i,j)
	 * assume route apply to single-depot VRP where 0 is the depot index
	 * @param i node i.
	 * @param j node j.
	 */
	public boolean contains(int i, int j){
		// if one is the depot
		if(i == 0 && (j == route.get(0) || j == route.get(route.size() - 1)))
			return true;
		if(j == 0 && (i == route.get(route.size()-1)|| i == route.get(0)))
			return true;

		int index = route.indexOf(i);
		if(index == -1)
			return false;
		if(index < route.size() - 1){
			if(route.get(index + 1 ) == j)
				return true;
		}
//		for(int temp = index + 1; temp < route.size() - 1; temp++){
//			if(route.get(temp) == i){
//				if(index < route.size() - 1){
//					if(route.get(index + 1 ) == j)
//						return true;
//				}
//			}
//		}
		index = route.indexOf(j);
		if(index == -1)
			return false;
		if(index < route.size() - 1){
			if(route.get(index + 1 ) == i)
				return true;
		}
//		for(int temp = index + 1; temp < route.size() - 1; temp++){
//			if(route.get(temp) == j){
//				if(index < route.size() - 1){
//					if(route.get(index + 1 ) == i)
//						return true;
//				}
//			}
//		}
		
		return false;
	}
	/**
	 * check if route contains node i
	 *
	 * @param i node i.
	 */
	public boolean contains(int i){
		return this.route.contains(i);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Route)) {
			return false;
		}

		Route that = (Route) other;
		// Custom equality check here.
		return this.route.equals(that.route)
				&& this.vehicleIndex==that.vehicleIndex;
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		hashCode = hashCode * 37 + this.route.hashCode();
		hashCode = hashCode * 37 + this.vehicleIndex;
		return hashCode;
	}

}

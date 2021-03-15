package RandCol;

import java.util.*;

public class Seperator2 {
    private Graph gStar;
    private Graph graph;

    public List<Cut> getCutPool() {
        return cutPool;
    }

    private List<Cut> cutPool;
    private int n;
    private int Q;


    public Seperator2(int n, List<Route> routes, int capacity){
        this.n = n;
        this.Q = capacity;
        this.gStar = new Graph(n, routes, true);
        this.cutPool = new ArrayList<>();
    }

    public void roundedCap(){
        UnionFind uf = new UnionFind(n);
        for(int i = 1; i < gStar.getSize(); i++){
            for(int j = i+1; j < gStar.getSize(); j++){
                if(gStar.graph[i][j]){
                    uf.union(i,j);
                }
            }
        }
        int size = uf.getCount();
        int[] node2comp = new int[n];
        Arrays.fill(node2comp, -1);
        int counter = 0;
        List<List<Integer>> components = new ArrayList<>();
        for(int i = 0; i < n; i++){
            components.add(new ArrayList<>());
        }
        for(int i = 1; i < gStar.getSize(); i++){
            int base = uf.find(i);
            components.get(base).add(i);
            if(node2comp[uf.find(i)] == -1){
                node2comp[i] = counter++;
            }else{
                node2comp[i] = node2comp[uf.find(i)];
            }
        }
        for(int i = 1; i < n; i++){
            if(components.get(i).size() <= 1) continue;
//            System.out.println(i+":"+components.get(i));
            checkRCI1(components.get(i));
        }
        //checkRCI2(gStar);



    }

    public void checkRCI1(List<Integer> comp){
        boolean[] set = new boolean[n];
        for(int i : comp){
            set[i] = true;
        }
        double tot = 0;
        double tot2 = 0;
        for(int i = 0; i < n; i++){
//            if(set[i]){
//                tot += gStar.x[0][i];
//            }else {
//                tot2 += gStar.x[0][i];
//            }
            for(int j = 0; j < n; j++){
                if(set[i] ^ set[j]){
                    if(set[j]) tot += gStar.x[i][j];
                    else tot2 += gStar.x[i][j];
                }
            }
        }
        int r = (int)Math.ceil(1.0*(comp.size())/Q);
        if(tot+1e-6 < r){
            System.out.println(comp+":"+tot+"-"+r);
            Cut cut = new Cut(set, r);
            cutPool.add(cut);
        }
//        r = (int)Math.ceil(1.0*(n-1-comp.size())/Q);
//        if(tot2 < r-1e-6){
//            boolean[] cset = new boolean[n];
//
//            for(int i = 1; i < n; i++) cset[i] = !set[i];
//            System.out.println(comp+":"+tot2+"-"+r);
//            Cut cut = new Cut(cset, r);
//            cutPool.add(cut);
//        }
    }


    public void checkRCI2(Graph gStar){
        double[][] graph = new double[n+1][n+1];
        for(int i = 1; i < n; i++){
            for(int j = 1; j < n; j++){
                graph[i][j] = gStar.x[i][j];
            }
        }
        for(int j = 1; j < n;j ++){
            graph[0][j] = Math.max(0, gStar.x[0][j] - 1.0/Q);
            graph[j][n] = Math.max(0, 1.0/Q - gStar.x[0][j]);
        }
        boolean[] set = minCut(graph, 0, n+1);
        for(int i = 0; i < n; i++)
            set[i] = !set[i];
        int count = 0;
        for(boolean b : set) if(b) count++;
        double tot = 0;
        double tot2 = 0;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(set[i] ^ set[j]){
                    if(set[j]) tot += gStar.x[i][j];
                    else tot2 += gStar.x[i][j];
                }
            }
        }
        int r = (int)Math.ceil(1.0*(count)/Q);
        if(tot < 2*r-1e-6){
            Cut cut = new Cut(set, 2*r);
            cutPool.add(cut);
        }



    }
    public List<Cut> generate(){
        roundedCap();
        return cutPool;
    }

    public static void main(String[] args){
        List<Integer> a1 = new ArrayList<>(Arrays.asList(3,2,1));
        List<Integer> a2 = new ArrayList<>(Arrays.asList(6));
        List<Integer> a3 = new ArrayList<>(Arrays.asList(4,5));
        Route r1= new Route(a1);

        Route r2= new Route(a2);
        Route r3= new Route(a3);
        r1.sol = 1;
        r2.sol = 0.1;
        r3.sol = 0.1;
        List<Route> rlst = new ArrayList<>(Arrays.asList(r1,r2,r3));
        Seperator2 test = new Seperator2(7, rlst, 3);
        test.roundedCap();
        System.out.println(test.cutPool.size());

    }

    public class Graph {
        private boolean[][] graph;
        private int size;
        private double[][] x;

        public boolean[][] getGraph() {
            return graph;
        }

        public void setGraph(boolean[][] graph) {
            this.graph = graph;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        /**
         * Constructor of support graph
         * @param n number of nodes
         * @param lst list of used route in current solution
         * @param removeDepot indicator whether graph contains depot node or not
         *
         */
        public Graph(int n, List<Route> lst, boolean removeDepot){
            this.size = n;
            this.x = new double[n][n];
            graph = new boolean[n][n];

            for(Route r : lst){
                for(int i = 0; i <= r.route.size(); i++){
                    int from = i==0?0:r.route.get(i-1);
                    int to = i==r.route.size()?0:r.route.get(i);
                    graph[from][to] = true;
                    this.x[from][to] += r.sol;
                }
            }
//            for(int i = 0; i < n; i++)
//                System.out.println(Arrays.toString(x[i]));
        }
    }



    public class UnionFind {
        private int count; // number of disjoint sets
        private int[] parent;

        public UnionFind(int n) {
            count = n;
            parent = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] != i) {
                parent[i] = find(parent[i]);
            }
            return parent[i];
        }

        public void union(int x, int y) {
            int rootx = find(x);
            int rooty = find(y);
            if (rootx != rooty) {
                if (rootx < rooty) {
                    parent[rooty] = rootx;
                }else{
                    parent[rootx] = rooty;
                }
                --count;
            }
        }

        public int getCount() {
            return count;
        }
    }



    private static boolean bfs(double[][] rGraph, int s,
                               int t, int[] parent) {

        // Create a visited array and mark
        // all vertices as not visited
        boolean[] visited = new boolean[rGraph.length];

        // Create a queue, enqueue source vertex
        // and mark source vertex as visited
        Queue<Integer> q = new LinkedList<Integer>();
        q.add(s);
        visited[s] = true;
        parent[s] = -1;

        // Standard BFS Loop
        while (!q.isEmpty()) {
            int v = q.poll();
            for (int i = 0; i < rGraph.length; i++) {
                if (rGraph[v][i] > 0 && !visited[i]) {
                    q.offer(i);
                    visited[i] = true;
                    parent[i] = v;
                }
            }
        }

        // If we reached sink in BFS starting
        // from source, then return true, else false
        return (visited[t] == true);
    }

    // A DFS based function to find all reachable
    // vertices from s. The function marks visited[i]
    // as true if i is reachable from s. The initial
    // values in visited[] must be false. We can also
    // use BFS to find reachable vertices
    private static void dfs(double[][] rGraph, int s,
                            boolean[] visited) {
        visited[s] = true;
        for (int i = 0; i < rGraph.length; i++) {
            if (rGraph[s][i] > 0 && !visited[i]) {
                dfs(rGraph, i, visited);
            }
        }
    }

    // Prints the minimum s-t cut
    private boolean[] minCut(double[][] graph, int s, int t) {
        int u,v;

        // Create a residual graph and fill the residual
        // graph with given capacities in the original
        // graph as residual capacities in residual graph
        // rGraph[i][j] indicates residual capacity of edge i-j
        double[][] rGraph = new double[graph.length][graph.length];
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                rGraph[i][j] = graph[i][j];
            }
        }

        // This array is filled by BFS and to store path
        int[] parent = new int[graph.length];

        // Augment the flow while tere is path from source to sink
        while (bfs(rGraph, s, t, parent)) {

            // Find minimum residual capacity of the edhes
            // along the path filled by BFS. Or we can say
            // find the maximum flow through the path found.
            double pathFlow = Double.POSITIVE_INFINITY;
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                pathFlow = Math.min(pathFlow, rGraph[u][v]);
            }

            // update residual capacities of the edges and
            // reverse edges along the path
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                rGraph[u][v] = rGraph[u][v] - pathFlow;
                rGraph[v][u] = rGraph[v][u] + pathFlow;
            }
        }

        // Flow is maximum now, find vertices reachable from s
        boolean[] isVisited = new boolean[graph.length];
        dfs(rGraph, s, isVisited);

        // Print all edges that are from a reachable vertex to
        // non-reachable vertex in the original graph
//        for (int i = 0; i < graph.length; i++) {
//            for (int j = 0; j < graph.length; j++) {
//                if (graph[i][j] > 0 && isVisited[i] && !isVisited[j]) {
//                    System.out.println(i + " - " + j);
//                }
//            }
//        }
        return isVisited;
    }

}

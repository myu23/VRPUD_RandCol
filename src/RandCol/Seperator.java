package RandCol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Seperator {
    private Graph gStar;
    private Graph graph;

    public List<Cut> getCutPool() {
        return cutPool;
    }

    private List<Cut> cutPool;
    private int n;
    private int Q;


    public Seperator(int n, List<Route> routes, int capacity){
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



    }

    public void checkRCI1(List<Integer> comp){
        boolean[] set = new boolean[n];
        for(int i : comp){
            set[i] = true;
        }
        double tot = 0;
        double tot2 = 0;
        for(int i = 1; i < n; i++){
            if(set[i]){
                tot += gStar.x[0][i];
            }else {
                tot2 += gStar.x[0][i];
            }
        }
        int r = (int)Math.ceil(1.0*(comp.size())/Q);
        if(tot < 2*r-1e-6){
            System.out.println(comp+":"+tot);
            Cut cut = new Cut(set, 2*r);
            cutPool.add(cut);
        }
        r = (int) Math.ceil(1.0*(n-1-comp.size())/Q);
        if(tot2 < 2*r-1e-6){
            boolean[] cset = new boolean[n];
            for(int i = 1; i < n; i++) cset[i] = !set[i];
            Cut cut = new Cut(cset, 2*r);
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
        Seperator test = new Seperator(7, rlst, 3);
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
                    graph[to][from] = true;
                    this.x[from][to] += r.sol;
                    this.x[to][from] += r.sol;
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




}

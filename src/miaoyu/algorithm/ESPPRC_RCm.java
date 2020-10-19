    /**
    * This class contains the algorithm to solve ESPPRC by Feillet et al.
    */

    package miaoyu.algorithm;

    import miaoyu.helper.Data;
    import miaoyu.helper.Route;

    import java.io.File;
    import java.util.*;

    public class ESPPRC_RCm {
    public Data data;
    public ArrayList<Label> labels;
    public Set<Route> solutionSet;
    public int[] color;
    public static int nColor ;
    public int nIter = 39;
    public static int colorComb;
    public static ArrayList<ArrayList<Integer>> subset;
    public static ArrayList<ArrayList<Integer>> superset;
    public static boolean[][] binary;


    public static void main(String[] args){
        //test();
    }

    public static void test1(){
        colorComb = (int) Math.pow(2, 4);
        subset = new ArrayList<>(colorComb);
        superset = new ArrayList<>(colorComb);
        for(int i =0; i < colorComb; i++){
            subset.add(new ArrayList<>());
            superset.add(new ArrayList<>());
        }
        findSubset();
        findSuperset();
        System.out.println(subset.toString());
        System.out.println(superset.toString());
        nColor=4;
        binary = new boolean[colorComb][nColor];
        for(int i = 0; i < colorComb; i++){
            String ib = Integer.toBinaryString(i);
            System.out.println(ib);
            for(int j = 0; j < ib.length(); j++){
                if(ib.charAt(ib.length()-1-j) == '1'){
                    binary[i][nColor-1-j] = true;
                }
            }
            System.out.println(i + " " + Arrays.toString(binary[i]));
        }
    }

//
//    public static void test(){
//            File file = new File("solomon_100/C101.txt");
//
//            Data data = new Data(file.toString(), 100,4);
//            data.generateInstance(201);
//            ESPPRC_RCm espprc = new ESPPRC_RCm(data);
//            long tic = System.currentTimeMillis();
//            espprc.solve();
//            long toc = System.currentTimeMillis();
//
//
//
//
//
//    }

    public ESPPRC_RCm(Data d){
        this.data = d;
        this.nColor = data.capacity;
        this.labels = new ArrayList<>(d.nNode * 2);
        this.solutionSet = new HashSet<>();
        this.colorComb = (int) Math.pow(2, nColor);
        this.subset = new ArrayList<>(colorComb);
        this.superset = new ArrayList<>(colorComb);
        for(int i =0; i < colorComb; i++){
            subset.add(new ArrayList<>());
            superset.add(new ArrayList<>());
        }
        findSubset();
        findSuperset();
        this.binary = new boolean[colorComb][nColor];
        for(int i = 0; i < colorComb; i++){
            String ib = Integer.toBinaryString(i);
            //System.out.println(ib);
            for(int j = 0; j < ib.length(); j++){
                if(ib.charAt(ib.length()-1-j) == '1'){
                    binary[i][nColor-1-j] = true;
                }
            }
            //System.out.println(i + " " + Arrays.toString(binary[i]));
        }
    }

    public void randomColor(){
        color = new int[data.nNode];
        for(int i = 1; i < data.nNode; i++){
            color[i] = (int) (Math.random() * nColor);
        }
    }

    public static void findSubset(){ //inclusive
        for(int i = 0; i < colorComb; i++){
            String bi = Integer.toBinaryString(i);
            //System.out.println(bi);
            for(int j = 0; j <= i; j++){
                boolean flag = true;
                String bj = Integer.toBinaryString(j);
                int len = bj.length();
                for(int l = 0; l < len; l++){
                    if(bi.charAt(bi.length()-1-l) < bj.charAt(bj.length()-1-l)){
                        flag = false; break;
                    }
                }
                if(flag)
                    subset.get(i).add(j);
            }
        }
    }
    public static void findSuperset(){
        for(int i = 0; i < colorComb; i++){
            for(int j = 0; j < colorComb; j++){
                if(subset.get(j).contains(i)){
                    superset.get(i).add(j);
                }
            }
        }
    }



    public class Label{
        public int endNode;
        public int prevIndex;
        public double cost;
        public double time;
        public int demand;
        public boolean dominated;
        public int nodeVisited;
        public int length;

        public Label(int a1, int a2, double a3, int a5, boolean a6, int a7){
            endNode = a1;
            prevIndex = a2;
            cost = a3;
            demand = a5;
            dominated = a6;
            nodeVisited = a7;
            length = 0;
        }
    }


    public void solve(int nbroute) {
        //initialization
        int i, j, currentIndex, currentNode;
        int nbsol;
        double t, tt;
        int d, dd;
        Label current;

        //list for active nodes
        TreeSet<Integer> U = new TreeSet<>();
        //list for solution labels
        Deque<Integer> P = new LinkedList<>();

        System.out.println("initialization "+labels.size());
        labels.add(new Label(0, -1, 0.0, 0, false, 0));    // first label: start from depot (client 0)
        U.add(0);

        // store the list of paths (idx of labels) ending at each node
        int[][] node2labels = new int[data.nNode + 1][colorComb];
        for(i = 0; i < node2labels.length; i++)
            for(j = 0; j < node2labels[0].length; j++)
                node2labels[i][j] = -1; //initialization
        node2labels[0][0] = 0;

        while (U.size() > 0) {
            //System.out.println("while labels size "+labels.size()+"; U: "+U.size()+"; P: "+P.size());
            //stop if found enough number of routes
            if (P.size() > nbroute) {
                //System.out.println("break here");
                break;
            }
            currentNode = U.pollFirst();
            //System.out.println("labels size "+labels.size()+"; U: "+U.size()+"; P: "+P.size());

            //System.out.println("current node "+currentNode);
    //            for (i = 0; i < data.nNode+1; i++){
    //                System.out.println(Arrays.toString(node2labels[i]));
    //            }
    //            System.out.println(" ");
//            for (i = 0; i < colorComb; i++){
//                System.out.println(Arrays.toString(binary[i]));
//            }
            //System.out.println(" ");
            if (currentNode == data.nNode) { // shortest path candidate to the depot!
                //System.out.println("path to depots!");
                for (int k = 0; k < colorComb; k++) {
                    int idx = node2labels[currentNode][k];
                    if (idx == -1) continue;
                    current = labels.get(idx);
                    if (current.cost < -1e-6) {                // SP candidate for the column generation
                        P.add(idx);
                    }

                }
                continue;
            }
            for (int k = colorComb-1; k >= 0; k--) {

                    currentIndex = node2labels[currentNode][k];
                    //System.out.println(currentIndex+" "+currentNode+" "+k);
                    if (currentIndex == -1) {
                        //System.out.println("end here");
                        continue; //skip empty label
                    }
                    current = labels.get(currentIndex);
                    for (i = 1; i < data.nNode + 1; i++) {
                        if(i==data.nNode ){
                            if(currentNode == 0) continue;

                            int idx = labels.size();
                            int col = current.nodeVisited;
                            Label nl = new Label(i, currentIndex, current.cost + data.cost[current.endNode][i], current.demand, false, col);
                            nl.length = current.length + 1;
                            // check if existing ones dominate new label
                            for (int id : superset.get(nl.nodeVisited)) {
                                if(node2labels[i][id] == -1) continue;
                                Label l2 = labels.get(node2labels[i][id]);
                                if ((nl.cost >= l2.cost) && (nl.demand >= l2.demand)) {
                                    nl.dominated = true;
                                }
                            }

                            if (!nl.dominated) {
                                // check if new label dominates exisiting ones
                                for (int id : subset.get(nl.nodeVisited)) {
                                    if(node2labels[i][id] == -1) continue;
                                    Label l2 = labels.get(node2labels[i][id]);
                                    if ((nl.cost <= l2.cost) && (nl.demand <= l2.demand)) {
                                        l2.dominated = true;
                                        node2labels[i][id] = -1;
                                    }
                                }

                                node2labels[i][nl.nodeVisited] = idx;
                                U.add(i);
                                labels.add(nl);


                            }

                        }else if (((!binary[k][color[i]]) && (data.cost[current.endNode][i] < 1e6))) {  // don't go back to a vertex already visited or along a forbidden edge
                            //System.out.printf("current node %d(%d), next node %d(%d)%n",currentNode, color[currentNode], i, color[i]);
                            //System.out.println(binary[k][color[i]]);
                            // demand
                            d = current.demand + data.demand[i];

                            // is feasible?
                            if (d <= data.capacity) {
                                int idx = labels.size();
                                int col = current.nodeVisited + (int) Math.pow(2, nColor-1-color[i]);

                                //System.out.printf("current index %d, current node %d(%d), next node %d(%d)%n",currentIndex,currentNode, color[currentNode], i, color[i]);
                                //System.out.println("new color "+col);
                                Label nl = new Label(i, currentIndex, current.cost + data.cost[current.endNode][i], d, false, col);
                                nl.length = current.length + 1;

                                // check dominance
                                //System.out.println("current node: "+currentNode+"next: "+i);
                                //System.out.println("super set: "+superset.get(nl.nodeVisited).toString());
                                // check if existing ones dominate new label
                                for (int id : superset.get(nl.nodeVisited)) {
                                    if(node2labels[i][id] == -1) continue;
                                    Label l2 = labels.get(node2labels[i][id]);
                                    if ((nl.cost >= l2.cost) && (nl.demand >= l2.demand)) {
                                        nl.dominated = true;
                                    }
                                }

                                if (!nl.dominated) {
                                    // check if new label dominates exisiting ones
                                    for (int id : subset.get(nl.nodeVisited)) {
                                        if(node2labels[i][id] == -1) continue;
                                        Label l2 = labels.get(node2labels[i][id]);
                                        if ((nl.cost <= l2.cost) && (nl.demand <= l2.demand)) {
                                            l2.dominated = true;
                                            node2labels[i][id] = -1;
                                        }
                                    }
                                    //System.out.println("2current node: "+currentNode+"next: "+i);

                                    node2labels[i][nl.nodeVisited] = idx;
                                    //System.out.println(i+","+idx);
                                    U.add(i);
                                    labels.add(nl);
                                    //System.out.println(nl.nodeVisited+" new idx:"+node2labels[i][nl.nodeVisited]);
                                    //System.out.println("labels size "+labels.size());
                                    //System.out.println("U size "+U.size());

                                }


                            }
                            //System.out.println("1U size "+U.size());

                        }
                        //System.out.println(k+" U size "+U.size());

                    }
                    //System.out.println(k+" 3U size "+U.size());



            }
            //System.out.println("End U size "+U.size());
        }

            // filtering: find the path from depot to the destination
            Integer lab;
            i = 0;
            while ((i < nbroute) && ((lab = P.pollFirst()) != null)) {
                Label s = labels.get(lab);


                    if (s.cost < -1e-4) {
                        // System.out.println(s.cost);
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        int path = s.prevIndex;
                        while (path > 0) {
                            temp.add(labels.get(path).endNode);
                            path = labels.get(path).prevIndex;
                        }
                        Collections.reverse(temp);
                        Route newroute = new Route(temp);
                        newroute.cost = s.cost;
                        System.out.println(newroute.route.toString());
                        solutionSet.add(newroute);
                        i++;
                    }



            }


        }




    public boolean solve(){
        for(int i = 0; i < nIter && solutionSet.size() < 30; i++){
        //for(int i = 0; i <  30; i++){
            randomColor();
            solve(30);

        }
        if(solutionSet.size() > 0){
            System.out.println("new routes count"+solutionSet.size());
            return true;
        }else{
            return false;
        }
    }

    public int findIdx(boolean[] color){
        int size = color.length;
        int result = 0;
        for(int i = 0; i < size; i++){
            if(color[size- 1 - i])
            result+=  (int) Math.pow(2, i);
        }
        return result;
    }
    }

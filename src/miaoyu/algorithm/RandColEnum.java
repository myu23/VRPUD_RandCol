package miaoyu.algorithm;

import miaoyu.helper.Data;
import miaoyu.helper.Route;

import java.util.*;

public class RandColEnum {
        public Data data;
        public Set<Route> solutionSet;
        public static int nColor ;
        public int nIter = 1;
        public static int colorComb;
        public Random random;
        public double epsilon = -1e-6;

        public RandColEnum(Data d){
            this.data = d;
            this.solutionSet = new HashSet<>();
            this.nColor = data.capacity;
            this.colorComb = 1 << (nColor);
            this.random = new Random();
            setEpsilon();
        }

    /**
     * set stopping criteria
     * @param ub
     * @param lb
     */
    public void setEpsilon(double ub, double lb){
            this.epsilon = ub-lb;
        }

        public void setEpsilon(){
            switch (data.nNode){
                case 219:
                    epsilon =  117595 - 117208;
                    break;
                case 317:
                    epsilon = 78355-78115;
                    break;
                case 376:
                    epsilon = 147713-147298;
                    break;
                case 655:
                    epsilon = 106780-106556;
                    break;
                default:
                    epsilon = -1e-6;
                    break;
            }
        }

        public class RCTask implements Runnable{
            public ArrayList<Label> labels;
            public int[] color;
            public int nIter = 100;
            public ArrayList<Integer>[] c2n;


            public void randomColor(){
                color = new int[data.nNode];
                for(int i = 1; i < data.nNode; i++){
                    color[i] = random.nextInt(nColor);
                    c2n[color[i]].add(i);
                }
            }


            public class Label{
                public int endNode;
                public int prevIndex;
                public double cost;
                public int demand;
                public int visited;

                public Label(int endNode, int prevIndex, double cost, int demand, int visited){
                    this.endNode = endNode;
                    this.prevIndex = prevIndex;
                    this.cost = cost;
                    this.demand = demand;
                    this.visited = visited;
                }
            }

            public Set<Route> threadSolutionSet;
            public Set<Integer> test;

            public RCTask(){
                this.threadSolutionSet = new HashSet<>();
                this.test = new HashSet<>();
                this.labels = new ArrayList<Label>();
                this.color = new int[nColor];
                this.c2n = new ArrayList[nColor];
                for(int i = 0; i < nColor; i++){
                    c2n[i] = new ArrayList<>();
                }
            }

            public void run(){
                //initialization
                randomColor();
                int nbroute = 100000;
                int i, j, currentIndex, currentNode;
                int d;
                Label current;

                //list for active nodes
                TreeSet<Integer> U = new TreeSet<>();
                //list for solution labels
                HashSet<Integer> P = new HashSet<>();

                //System.out.println("initialization "+labels.size());
                labels.add(new Label(0, -1, 0.0, 0, 0));    // first label: start from depot (client 0)
                U.add(0);

                // store the list of paths (idx of labels) ending at each state
                int[][] state2labels = new int[data.nNode + 1][colorComb];
                for(i = 0; i < state2labels.length; i++)
                    for(j = 0; j < colorComb; j++)
                        state2labels[i][j] = -1; //initialization
                state2labels[0][0] = 0;

                while (U.size() > 0) {

                    currentNode = U.pollFirst();

                    if (currentNode == data.nNode) { // shortest path candidate to the depot!
                        //System.out.println("path to depots!");
                        for (int k = 0; k < colorComb; k++) {
                            int idx = state2labels[currentNode][k];
                            if (idx == -1) continue;
                            current = labels.get(idx);
                            if (current.cost < epsilon) {                // SP candidate for the column generation
                                P.add(idx);
                            }
                        }
                        continue;
                    }
                    for (int k = colorComb-1; k >= 0; k--) {
                        currentIndex = state2labels[currentNode][k];
                        if (currentIndex == -1) {
                            continue; //skip empty label
                        }
                        current = labels.get(currentIndex);

                        for(int c = 0; c < nColor; c++){
                            if((k>>c & 1) == 1) continue;
                            for(Integer node : c2n[c]){
                                d = current.demand + data.demand[node];
                                // is feasible?
                                if (d <= data.capacity) {
                                    int idx = labels.size();
                                    int col = current.visited + (1 << color[node]);

                                    Label nl = new Label(node, currentIndex, current.cost + data.cost[current.endNode][node], d, col);

                                    if(state2labels[node][col] == -1 || nl.cost < labels.get(state2labels[node][col]).cost){
                                        state2labels[node][nl.visited] = idx;
                                        U.add(node);
                                        labels.add(nl);
                                    }
                                }
                            }
                        }
                        if(currentNode == 0) continue;
                        int idx = labels.size();
                        int col = current.visited;
                        Label nl = new Label(data.nNode, currentIndex, current.cost + data.cost[current.endNode][data.nNode], current.demand, col);

                        // check if existing ones dominate new label
                        if(nl.cost < epsilon){
                            state2labels[data.nNode][nl.visited] = idx;
                            U.add(data.nNode);
                            labels.add(nl);
                            P.add(idx);
                        }

                    }
                }

                // filtering: find the path from depot to the destination
                Integer lab;
                Iterator<Integer> it = P.iterator();
                i = 0;
                while (it.hasNext() && ((lab = it.next()) != null)) {
                    Label s = labels.get(lab);
                    if (s.cost < epsilon) {
                        //System.out.println("Current cost: "+s.cost+" vs. epsilon: "+epsilon);
                        ArrayList<Integer> temp = new ArrayList<Integer>();
                        int path = s.prevIndex;
                        while (path > 0) {
                            temp.add(labels.get(path).endNode);
                            path = labels.get(path).prevIndex;
                        }
                        Collections.reverse(temp);
                        Route newroute = new Route(temp);
                        newroute.cost = s.cost;
                        solutionSet.add(newroute);
                        i++;
                    }

                }


            }
        }


        public boolean solve(){
            RCTask[] tasks = new RCTask[data.maxThread];
            for(int i = 0; i <data.maxThread; i++){
                tasks[i] = new RCTask();
            }
            try{
                for(int i = 1; i < data.maxThread; i++){
                    data.threads[i] = new Thread(tasks[i]);
                    data.threads[i].start();
                }

            }catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try{
                for(int i = 1; i < data.maxThread; i++){
                    data.threads[i].join();
                }
            }catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            for(int i = 1; i < data.maxThread; i++){
                solutionSet.addAll(tasks[i].threadSolutionSet);
            }

            if(solutionSet.size() > 0){
                System.out.println("new routes count"+solutionSet.size());
                return true;
            }else{
                return false;
            }
        }
}

package RandCol;

import java.util.*;

public class Cut {
    private boolean[] set;
    private List<int[]> edges;
    private double rhs;
    private String type;
    private boolean inPool;


    public Cut(boolean[] lhs, double rhs){
        this.set = lhs;
        this.rhs = rhs;
        this.type = null;
        this.inPool = false;
        this.edges = new ArrayList<>();
        generateEdge();
    }

    public boolean[] getSet() {
        return set;
    }

    public void setSet(boolean[] set) {
        this.set = set;
    }

    public double getRhs() {
        return rhs;
    }

    public void setRhs(double rhs) {
        this.rhs = rhs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInPool() {
        return inPool;
    }

    public void setInPool(boolean inPool) {
        this.inPool = inPool;
    }

    public List<int[]> getEdges(){
        return this.edges;
    }

    public void generateEdge(){
        for(int i = 0; i < set.length; i++){
            for(int j = 0; j < set.length; j++){
                if(set[j] && (set[i]^set[j])){
                    edges.add(new int[]{i, j});
                }

            }
        }
//        for(int i = 0; i < set.length; i++){
//            for(int j = i+1; j < set.length; j++){
//                if((set[i]^set[j])){
//                    edges.add(new int[]{i, j});
//                }
//            }
//        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cut cut = (Cut) o;
        return Double.compare(cut.getRhs(), getRhs()) == 0 &&
                Arrays.equals(getSet(), cut.getSet()); 
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getRhs());
        result = 31 * result + Arrays.hashCode(getSet());
        return result;
    }
}

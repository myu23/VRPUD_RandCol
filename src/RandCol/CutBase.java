package RandCol;
import java.util.*;
public class CutBase {
    public void setCapInSet(int capInSet) {
        this.capInSet = capInSet;
    }

    public void setRhs(double rhs) {
        this.rhs = rhs;
    }

    public void setnCustomer(int nCustomer) {
        this.nCustomer = nCustomer;
    }

    public int getCapInSet() {
        return capInSet;
    }

    public double getRhs() {
        return rhs;
    }

    public int getnCustomer() {
        return nCustomer;
    }

    public int[] getNodeList() {
        return nodeList;
    }

    public void setNodeList(int[] nodeList) {
        this.nodeList = nodeList;
    }

    public boolean[] getInSet() {
        return inSet;
    }

    public void setInSet(boolean[] inSet) {
        this.inSet = inSet;
    }

    private int[] nodeList;
    private boolean[] inSet;
    private int capInSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CutBase cutBase = (CutBase) o;
        return getCapInSet() == cutBase.getCapInSet() &&
                Double.compare(cutBase.getRhs(), getRhs()) == 0 &&
                getnCustomer() == cutBase.getnCustomer() &&
                Arrays.equals(getNodeList(), cutBase.getNodeList()) &&
                Arrays.equals(getInSet(), cutBase.getInSet());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getCapInSet(), getRhs(), getnCustomer());
        result = 31 * result + Arrays.hashCode(getNodeList());
        result = 31 * result + Arrays.hashCode(getInSet());
        return result;
    }

    private double rhs;
    private int nCustomer;

    public CutBase(int[] nodes, int n, double rhs){
        this.nodeList = nodes;
        this.capInSet = nodes.length;
        this.nCustomer = n;
        this.rhs = rhs;
        this.inSet = new boolean[n];
        for(int i : nodes) inSet[i] = true;
    }
}

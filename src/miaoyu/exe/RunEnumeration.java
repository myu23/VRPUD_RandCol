package miaoyu.exe;

import miaoyu.algorithm.ColumnGeneration_PMX;
import miaoyu.algorithm.ColumnGeneration_RCMX;
import miaoyu.algorithm.ColumnGeneration_RCMX3;
import miaoyu.algorithm.ColumnGeneration_RCXenumeration;
import miaoyu.helper.Data;
import miaoyu.helper.Functions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RunEnumeration {
    public static Runtime rt = Runtime.getRuntime();

    public static void main(String[] args)throws InterruptedException, NumberFormatException, IOException{
        File file = new File("X_unit/");
        String[] fileName = new String[]{ "X_unit\\X-n219-k73.vrp", "X_unit\\X-n219-k73.vrp", "X_unit\\X-n317-k53.vrp","X_unit\\X-n376-k94.vrp","X_unit\\X-n655-k131.vrp"};
        int[] c = new int[] {3, 3, 6, 4, 5};
        for(int l = 0; l < 5; l++){
                Data data = new Data(fileName[l], c[l]);
                record_randcol_mx2(data);

        }
    }


    public static void record_randcol_mx2(Data data){
        int counter = 0;

        ColumnGeneration_RCXenumeration cg = new ColumnGeneration_RCXenumeration(data);


        try{
            cg.solve();
            cg.solveMIP();
        }catch (Exception e){
            System.out.println(e.toString());
        }


        ArrayList<String> output = new ArrayList<>();
        output.add(data.fileName);
        output.add(Integer.toString(data.nNode));
        output.add(Integer.toString(cg.nIter));
        output.add(Integer.toString(cg.nColumns));
        output.add(Double.toString(cg.lowerbound));
        output.add(Double.toString(cg.upperbound));
        output.add(Double.toString(cg.lptime/1000.0));
        output.add(Double.toString(cg.iptime/1000.0));
        output.add(Double.toString(cg.etime/1000.0));
        output.add(Integer.toString(cg.count));
        Functions.writeDataCSV("Data/VRPUDX/RandCol-multi/", data.fileName+"-q"+data.capacity+"-e", output);
        rt.gc();


    }

}

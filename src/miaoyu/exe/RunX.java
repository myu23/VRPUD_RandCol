package miaoyu.exe;

import miaoyu.algorithm.*;
import miaoyu.helper.Data;
import miaoyu.helper.DataMD;
import miaoyu.helper.Functions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class RunX {
    public static Runtime rt = Runtime.getRuntime();

    public static void main(String[] args)throws InterruptedException, NumberFormatException, IOException{
        File file = new File("X_unit/");

        for(int l = 3; l < 6; l++){
            for(File f : file.listFiles()) {
            System.out.println(f.toString());
                Data data = new Data(f.toString(), l);
                //record_pulse_mx(data);
                //record_randcol_mx(data);
                record_randcol_mx2(data);

            }
        }
    }

    public static void record_pulse_mx(Data data){
        int counter = 0;

        ColumnGeneration_PMX cg = new ColumnGeneration_PMX(data);




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
        //output.add(Double.toString(cg.timerBeforeLast/1000.0));
        output.add(Double.toString(cg.lptime/1000.0));
        output.add(Double.toString(cg.iptime/1000.0));
        Functions.writeDataCSV("Data/VRPUDX/Pulse-multi/", data.fileName+"-q"+data.capacity, output);
        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
        rt.gc();


    }


    public static void record_randcol_mx(Data data){
            int counter = 0;

                ColumnGeneration_RCMX cg = new ColumnGeneration_RCMX(data);




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
                //output.add(Double.toString(cg.timerBeforeLast/1000.0));
                output.add(Double.toString(cg.lptime/1000.0));
                output.add(Double.toString(cg.iptime/1000.0));
                Functions.writeDataCSV("Data/VRPUDX/RandCol-multi/", data.fileName+"-q"+data.capacity, output);
                //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                rt.gc();


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
        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
        rt.gc();


    }

}

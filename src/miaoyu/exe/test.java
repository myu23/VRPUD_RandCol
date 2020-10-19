package miaoyu.exe;
import miaoyu.helper.*;
import miaoyu.algorithm.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class test {
    public static void main(String[] args){

        String fileName = "WayneData/Wayne5.csv";
        DataMD data = new DataMD(fileName, 103, 3, 4);
        System.out.println("here");
        ColumnGeneration_RCMDe cg = new ColumnGeneration_RCMDe(data);

        long tic = System.currentTimeMillis();
        long toc1 = System.currentTimeMillis();
        long toc2 = System.currentTimeMillis();

        try{
            cg.solve();
            toc1 = System.currentTimeMillis();
            cg.solveMIP();
            toc2 = System.currentTimeMillis();
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
        output.add(Double.toString((toc1 - tic)/1000.0));
        output.add(Double.toString(cg.iptime/1000.0));
        System.out.println(output.toString());
        Functions.writeDataCSV("Data/UCVRP"+4+"/RCMD/", data.nDepot+"-"+data.nNode+"-e", output);

        //System.out.println("Solution time for general algorithm: " + (double)(toc2-tic2)/1000.0);
        //System.out.println("Solution time for randomized algorithm: " + (double)(toc - tic)/1000.0);


    }

    public static void record_randcol_md(){
        {
            File file = new File("WayneData/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith(".csv")){
                    continue;
                }
                for(int l = 4; l < 5; l++){
                    for(int ndpt = 1; ndpt<=5; ndpt+=2)
                        for(int n = 100; n <=500; n=n+50){
                            //int ndpt = 5;
                            DataMD data = new DataMD(f.toString(), n+ndpt, ndpt, l);
                            ColumnGeneration_RCMD2 cg = new ColumnGeneration_RCMD2(data);


                            long tic = System.currentTimeMillis();
                            long toc1 = System.currentTimeMillis();
                            long toc2 = System.currentTimeMillis();

                            try{
                                cg.solve();
                                toc1 = System.currentTimeMillis();
                                cg.solveMIP();
                                toc2 = System.currentTimeMillis();
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
                            output.add(Double.toString((toc1 - tic)/1000.0));
                            output.add(Double.toString((toc2 - toc1)/1000.0));
                            System.out.println(output.toString());
                            Functions.writeDataCSV("Data/UCVRP"+l+"/RCMD/", data.nDepot+"-"+data.nNode+"-ii", output);
                            //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);

                        }
                }

//            break;
            }
        }
    }

}

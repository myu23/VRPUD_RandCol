package miaoyu.exe;
import miaoyu.algorithm.ColumnGeneration;
import miaoyu.algorithm.ColumnGeneration_RC;
import miaoyu.helper.Data;
import static miaoyu.helper.Functions.*;

import java.util.*;
import java.io.*;


public class CleanResult {

    public static void main(String[] args){
//        String[] types = {"RandCol-multi"};
//        for(int i = 3; i < 7; i++){
//            for(String t : types){
//                summary(i,t);
//            }
//        }
        //summary("RandCol-multi");
        summary(4, "RCMD");

        //combine(4);
    }


    public static void summary( String type){
        File file = new File("Data/VRPUDX"+"/"+type+"/X_unit/");
        System.out.println(Arrays.toString(file.listFiles()));
        ArrayList<String> summary = new ArrayList<>();
        for(File f : file.listFiles()) {
            if(!f.toString().endsWith("-e.csv"))
                continue;
            System.out.println(f.toString());

            ArrayList<String[]> temp = readDataCSV(f.toString());
            System.out.println(temp.size());
            String output="";

            for(int i = 0; i < temp.size(); i++){
                output = output + temp.get(i)[0] + ",";
            }
            System.out.println("!"+output);
            summary.add(output);

        }
        writeDataCSV("Data/VRPUDX/"+type+"/","esummary_"+type, summary);
    }


    public static void summary(int l, String type){
        //File file = new File("Data/UCVRP"+l+"/"+type+"/solomon_100/");
        File file = new File("Data/UCVRP"+l+"/"+type+"/");

        System.out.println(Arrays.toString(file.listFiles()));
        ArrayList<String> summary = new ArrayList<>();
        for(File f : file.listFiles()) {
            if(!f.toString().endsWith("-e.csv"))
                continue;
            System.out.println(f.toString());

            ArrayList<String[]> temp = readDataCSV(f.toString());
            System.out.println(temp.size());
            String output="";

            for(int i = 0; i < temp.size(); i++){
                output = output + temp.get(i)[0] + ",";
            }
            System.out.println("!"+output);
            summary.add(output);

        }
        writeDataCSV("Data/UCVRP"+l+"/"+type+"/","esummary_"+type, summary);
    }

    public static void combine(int l){
        String[] types = {"Pulse", "Pulse-multi", "RandCol", "RandCol-multi"};
        ArrayList<String> summary = new ArrayList<>();

        for(String t : types){
            File file = new File("Data/UCVRP"+l+"/"+t+"/solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("summary1.csv"))
                    continue;
                System.out.println(f.toString());

                ArrayList<String[]> temp = readDataCSV(f.toString());
                System.out.println(temp.size());

                for(int i = 0; i < temp.size(); i++){
                    String output="";
                    for(int j = 0 ; j < temp.get(i).length; j++)
                        output = output + temp.get(i)[j] + ",";
                    summary.add(output);
                }
                //System.out.println("!"+output);
                summary.add("");

            }
        }
        writeDataCSV("Data/UCVRP"+l+"/","summary1", summary);

    }

}

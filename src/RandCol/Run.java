package RandCol;

import java.util.*;
import java.io.*;
import static miaoyu.helper.Functions.*;

public class Run{

    public static void main(String[] args){

        //summaryRand(5);
        summaryPulse(5);
    }

    public static void summaryRand(int l){
        File file = new File("Results/RandCol/");
        System.out.println(Arrays.toString(file.listFiles()));
        ArrayList<String> summary = new ArrayList<>();
        boolean flag = true;
        for(File f : file.listFiles()) {
            if(!f.toString().endsWith("-K"+l+".csv"))
                continue;
            System.out.println(f.toString());

            ArrayList<String[]> temp = readDataCSV(f.toString());
            System.out.println(temp.size());
            String output="";
            if(flag){
                for(int i = 0; i < temp.size(); i++){
                    output = output + temp.get(i)[0] + ",";
                }
                summary.add(output);
                flag = false;
            }
            output = "";
            for(int i = 0; i < temp.size(); i++){
                output = output + temp.get(i)[1] + ",";
            }
            System.out.println("!"+output);
            summary.add(output);
        }
        writeDataCSV("Results/", "randcol_summary_k"+l, summary);
    }

    public static void summaryPulse(int l){
        File file = new File("Results/Pulse/");
        System.out.println(Arrays.toString(file.listFiles()));
        ArrayList<String> summary = new ArrayList<>();
        boolean flag = true;
        for(File f : file.listFiles()) {
            if(!f.toString().endsWith("-K"+l+".csv"))
                continue;
            System.out.println(f.toString());

            ArrayList<String[]> temp = readDataCSV(f.toString());
            System.out.println(temp.size());
            String output="";
            if(flag){
                for(int i = 0; i < temp.size(); i++){
                    output = output + temp.get(i)[0] + ",";
                }
                summary.add(output);
                flag = false;
            }
            output = "";
            for(int i = 0; i < temp.size(); i++){
                output = output + temp.get(i)[1] + ",";
            }
            System.out.println("!"+output);
            summary.add(output);
        }
        writeDataCSV("Results/", "pulse_summary_k"+l, summary);
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
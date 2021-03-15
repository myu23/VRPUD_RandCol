package RandCol;



import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class test {
    public static void main(String[] args){

        xinstance();
    }

    public static void xP(){
        try{
            String folder = "Instances\\X_unit\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {

                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 4; l++){
                    Data data = new Data(folder, filename, l);
                    if(data.nNode != 219) continue;
                    CG_Pulse cg = new CG_Pulse(data);
                    cg.solve();
                    //cg.solveMIP();
                    ArrayList<String> output = new ArrayList<>();
                    output.add("FileName,"+data.fileName);
                    output.add("nNode,"+Integer.toString(data.nNode));
                    output.add("nIter,"+Integer.toString(cg.nIter));
                    output.add("nCols,"+Integer.toString(cg.nColumns));
                    output.add("LB,"+Double.toString(cg.lowerbound));
                    output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                    output.add("UB_2,"+Double.toString(cg.upperbound));
                    output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                    output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                    output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                    output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                    output.add("nEnumCols,"+Integer.toString(cg.count));
                    Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                }

            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void solomon1(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("101.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 6; l++){
                    for(int n = 51; n <=101; n=n+10){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_RC cg = new CG_RC(data);
                        cg.solve();
                        cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/RandCol/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }





    public static void solomon2(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("121.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 6; l++){
                    for(int n = 111; n <=151; n=n+10){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_RC cg = new CG_RC(data);
                        cg.solve();
                        cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/RandCol/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void solomon3(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("161.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 5; l++){
                    for(int n = 201; n <=351; n=n+50){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_RC cg = new CG_RC(data);
                        cg.solve();
                        cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/RandCol/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }


    public static void solomon1P(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("101.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 6; l++){
                    for(int n = 51; n <=101; n=n+10){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_Pulse cg = new CG_Pulse(data);
                        cg.solve();
                        //cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void solomon2P(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("121.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 6; l++){
                    for(int n = 111; n <=151; n=n+10){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_Pulse cg = new CG_Pulse(data);
                        cg.solve();
                        //cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void solomon3P(){
        try{
            String folder = "Instances\\solomon_100\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                if(!f.toString().endsWith("161.txt")){
                    continue;
                }
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 5; l++){
                    for(int n = 201; n <=351; n=n+50){
//                        Data data = new Data(folder, filename, l);
                        Data data = new Data(folder, filename, n, l);
                        CG_Pulse cg = new CG_Pulse(data);
                        cg.solve();
                        //cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }



    public static void xinstance(){
        try{
            String folder = "Instances\\X_unit\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {

                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 4; l++){
                        Data data = new Data(folder, filename, l);
                        CG_RC cg = new CG_RC(data);
                        cg.solve();
                        cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        output.add("FileName,"+data.fileName);
                        output.add("nNode,"+Integer.toString(data.nNode));
                        output.add("nIter,"+Integer.toString(cg.nIter));
                        output.add("nCols,"+Integer.toString(cg.nColumns));
                        output.add("LB,"+Double.toString(cg.lowerbound));
                        output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                        output.add("UB_2,"+Double.toString(cg.upperbound));
                        output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                        output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                        output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                        output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                        output.add("nEnumCols,"+Integer.toString(cg.count));
                        Functions.writeDataCSV("Results/RandCol/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                        break;
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    public static void xinstanceP(){
        try{
            String folder = "Instances\\X_unit\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {

                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 5; l < 6; l++){
                    Data data = new Data(folder, filename, l);
                    CG_Pulse cg = new CG_Pulse(data);
                    cg.solve();
                    //cg.solveMIP();
                    ArrayList<String> output = new ArrayList<>();
                    output.add("FileName,"+data.fileName);
                    output.add("nNode,"+Integer.toString(data.nNode));
                    output.add("nIter,"+Integer.toString(cg.nIter));
                    output.add("nCols,"+Integer.toString(cg.nColumns));
                    output.add("LB,"+Double.toString(cg.lowerbound));
                    output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                    output.add("UB_2,"+Double.toString(cg.upperbound));
                    output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                    output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                    output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                    output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                    output.add("nEnumCols,"+Integer.toString(cg.count));
                    Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                }

            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void xinstanceP34(){
        try{
            String folder = "Instances\\X_unit\\";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {

                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                for(int l = 3; l < 5; l++){
                    Data data = new Data(folder, filename, l);
                    if(data.nNode < 850) continue;
                    CG_Pulse cg = new CG_Pulse(data);
                    cg.solve();
                    //cg.solveMIP();
                    ArrayList<String> output = new ArrayList<>();
                    output.add("FileName,"+data.fileName);
                    output.add("nNode,"+Integer.toString(data.nNode));
                    output.add("nIter,"+Integer.toString(cg.nIter));
                    output.add("nCols,"+Integer.toString(cg.nColumns));
                    output.add("LB,"+Double.toString(cg.lowerbound));
                    output.add("UB_1,"+Double.toString(cg.upperbound_temp));
                    output.add("UB_2,"+Double.toString(cg.upperbound));
                    output.add("t_lb,"+Double.toString(cg.lbTime/1000.0));
                    output.add("t_ub1,"+Double.toString(cg.ub0Time/1000.0));
                    output.add("t_enum,"+Double.toString(cg.etime/1000.0));
                    output.add("t_ub2,"+Double.toString(cg.ubTime/1000.0));
                    output.add("nEnumCols,"+Integer.toString(cg.count));
                    Functions.writeDataCSV("Results/Pulse/", data.fileName+"-N"+data.nNode+"-K"+data.capacity, output);
                }

            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

}

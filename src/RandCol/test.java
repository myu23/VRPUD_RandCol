package RandCol;



import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class test {
    public static void main(String[] args){
        solomon();
    }
    public static void solomon(){
        try{
            String folder = "Instances/solomon_100/";
            File file = new File(folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                //System.out.println(f.toString());
                if(!f.toString().endsWith("RC161.txt")){
                    continue;
                }
                String[] temp = f.toString().split("/");
                String filename = temp[temp.length-1];
                for(int l = 4; l < 5; l++){
                    for(int n = 201; n <=201; n=n+50){
                        Data data = new Data(folder, filename, n,l);
//                        if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
//                            continue;
//                        }
                        ColumnGeneration_RCM2 cg = new ColumnGeneration_RCM2(data);
                        cg.solve();
                        cg.solveMIP();
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
                        Functions.writeDataCSV("Results/", data.fileName, output);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void xinstance(){
        try{
            String folder = "X_unit/";
            File file = new File("Instances/"+folder);
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                String[] temp = f.toString().split("\\\\");
                String filename = temp[temp.length-1];
                System.out.println(filename);
                for(int l = 4; l < 5; l++){
                        Data data = new Data("Instances/"+folder, filename, l);
//                      if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
//                          continue;
//                      }
                        ColumnGeneration_RCM cg = new ColumnGeneration_RCM(data);
                        cg.solve();
                        cg.solveMIP();
                        ArrayList<String> output = new ArrayList<>();
                        System.out.println(data.fileName);
//                        output.add(data.fileName);
//                        output.add(Integer.toString(data.nNode));
//                        output.add(Integer.toString(cg.nIter));
//                        output.add(Integer.toString(cg.nColumns));
//                        output.add(Double.toString(cg.lowerbound));
//                        output.add(Double.toString(cg.upperbound));
//                        output.add(Double.toString(cg.lptime/1000.0));
//                        output.add(Double.toString(cg.iptime/1000.0));
//                        output.add(Double.toString(cg.etime/1000.0));
//                        output.add(Integer.toString(cg.count));
//                        Functions.writeDataCSV("Results/"+folder, filename, output);
                        break;

                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

//    public static double getUpperbound(int k, String fn, int n){
//        ArrayList<String[]> rawData = new ArrayList<String[]>();
//        String csvFile = "Data\\UCVRP"+k+"\\RandCol-multi\\"+fn+"-"+n+"-e.csv";
//        BufferedReader br = null;
//        String line = "";
//        String cvsSplitBy = ",";
//        double ub = 0;
//        try{
//            br = new BufferedReader(new FileReader(csvFile));
//            for(int i = 0; i < 6; i++)
//                line = br.readLine();
//            ub = Double.parseDouble(line);
//            return ub+0.5;
//        }catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return 0;
//    }

}

package miaoyu.exe;
import java.util.*;
import java.io.*;
import miaoyu.algorithm.*;
import miaoyu.helper.*;
import miaoyu.helper.Functions.*;
import java.lang.Runtime;
public class Run {
    public static Runtime rt = Runtime.getRuntime();

    public static void main(String[] args)throws InterruptedException, NumberFormatException, IOException{
        //record_pulse();
        //record_pulse_md();
        //record_randcol_md();

//
        //record_multi();
        record_multi2();
////
        //record_multi3();
        //record_multi4();

//        //record();
        //record_pulse_mx();
        //record_randcol_mx();
        //record_pulse_m();
//        File file = new File("solomon_100/");
//        System.out.println(Arrays.toString(file.listFiles()));
//        int counter = 0;
//        for(File f : file.listFiles()) {
//            System.out.println(f.toString());
//            if(!f.toString().endsWith(".csv")){
//                chj jmnb  ,l./ /l;ontinue;
//            }
//            for(int l = 4; l < 5; l++) {
//                for (int n = 100; n <= 100; n = n + 50) {
//                    int ndpt = 1;
//                    DataMD data = new DataMD(f.toString(), n + ndpt, ndpt, l);
////                    for(int i = 0; i < 50; i++)
////                        System.out.println(Arrays.toString(data.distance[0][i]));
//                }
//            }
//        }
    }

    public static void record(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("121.txt")){
                    continue;
                }
                for(int l = 4; l < 7; l++){
                    for(int n = 111; n <=151; n=n+10){
                        Data data = new Data(f.toString(), n,l);
                        ColumnGeneration_RCM cg = new ColumnGeneration_RCM(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

                        long tic = System.currentTimeMillis();
                        long toc1 = System.currentTimeMillis();
                        long toc2 = System.currentTimeMillis();

                        try{
                            cg.solve();
                            toc1 = System.currentTimeMillis();
                            cg.solveMIP();
                            toc2 = System.currentTimeMillis();
                        }catch (Exception e){

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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/RandCol/", data.fileName+"-"+n, output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }

    public static double getUpperbound(int k, String fn, int n){
        ArrayList<String[]> rawData = new ArrayList<String[]>();
        String csvFile = "Data\\UCVRP"+k+"\\RandCol-multi\\"+fn+"-"+n+"-e.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        double ub = 0;
        try{
            br = new BufferedReader(new FileReader(csvFile));
            for(int i = 0; i < 6; i++)
                line = br.readLine();
            ub = Double.parseDouble(line);
            return ub+0.5;
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static void record_multi(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("C161.txt")){
                    continue;
                }
                for(int l = 3; l < 4; l++){
                    for(int n = 201; n <=601; n=n+50){
                        Data data = new Data(f.toString(), n,l);
                        if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
                            continue;
                        }
                        ColumnGeneration_RCMe cg = new ColumnGeneration_RCMe(data);
                        System.out.println(cg.getUpperbound());
                        //ColumnGeneration cg = new ColumnGeneration(data);



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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/RandCol-multi/", data.fileName+"-"+n+"-e", output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }
    public static void record_multi2(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("161.txt")){
                    continue;
                }
                for(int l = 4; l < 5; l++){
                    for(int n = 201; n <=351; n=n+50){
                        Data data = new Data(f.toString(), n,l);
                        if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
                            continue;
                        }
                        ColumnGeneration_RCMe cg = new ColumnGeneration_RCMe(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/RandCol-multi/", data.fileName+"-"+n+"-e", output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }


    public static void record_multi3(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("101.txt")){
                    continue;
                }
                for(int l = 3; l < 7; l++){
                    for(int n = 51; n <=101; n=n+10){
                        Data data = new Data(f.toString(), n,l);
                        if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
                            continue;
                        }
                        ColumnGeneration_RCMe cg = new ColumnGeneration_RCMe(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

                        long tic = System.currentTimeMillis();
                        long toc1 = System.currentTimeMillis();
                        long toc2 = System.currentTimeMillis();

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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/RandCol-multi/", data.fileName+"-"+n+"-e", output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }
    public static void record_multi4(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("121.txt")){
                    continue;
                }
                for(int l = 3; l < 7; l++){
                    for(int n = 111; n <=151; n=n+10){
                        Data data = new Data(f.toString(), n,l);
                        if(getUpperbound(data.capacity, data.fileName, data.nNode) > 10){
                            continue;
                        }
                        ColumnGeneration_RCMe cg = new ColumnGeneration_RCMe(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/RandCol-multi/", data.fileName+"-"+n+"-e", output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }
    public static void record_pulse(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith(".txt")){
                    continue;
                }
                for(int l = 4; l < 6; l++){
                    for(int n = 161; n <=201; n=n+10){
                        Data data = new Data(f.toString(), n,l);
                        ColumnGeneration cg = new ColumnGeneration(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

                        long tic = System.currentTimeMillis();
                        try{
                            cg.solve();
                        }catch (Exception e){

                        }
                        long toc = System.currentTimeMillis();

                        ArrayList<String> output = new ArrayList<>();
                        output.add(data.fileName);
                        output.add(Integer.toString(data.nNode));
                        output.add(Integer.toString(cg.nIter));
                        output.add(Integer.toString(cg.nColumns));
                        output.add(Double.toString(cg.lowerbound));
                        //output.add(Double.toString(cg.timerBeforeLast/1000.0));
                        output.add(Double.toString((toc - tic)/1000.0));
                        Functions.writeDataCSV("Data/UCVRP"+l+"/Pulse/", data.fileName+"-"+n, output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }

    public static void record_pulse_m(){
        {
            File file = new File("solomon_100/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());
                if(!f.toString().endsWith("R121.txt")){
                    continue;
                }
                for(int l = 4; l < 5; l++){

//                    if(!f.toString().endsWith("161.txt")){
//                        continue;
//                    }
                    System.out.println("here!");

                    for(int n = 111; n <=151; n=n+10){
                        Data data = new Data(f.toString(), n,l);
                        ColumnGeneration_multi cg = new ColumnGeneration_multi(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

                        long tic = System.currentTimeMillis();
                        long toc1 = System.currentTimeMillis();
                        long toc2 = System.currentTimeMillis();

                        try{
                            cg.solve();
                            toc1 = System.currentTimeMillis();
                            cg.solveMIP();
                            toc2 = System.currentTimeMillis();
                        }catch (Exception e){

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
                        Functions.writeDataCSV("Data/UCVRP"+l+"/Pulse/", data.fileName+"-"+n, output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

            break;
            }
        }
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
                        ColumnGeneration_RCMDe cg = new ColumnGeneration_RCMDe(data);

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
                            Functions.writeDataCSV("Data/UCVRP"+4+"/RCMD/", data.nDepot+"-"+data.nNode+"-e", output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }

    public static void record_randcol_mde(){
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
                    for(int ndpt = 3; ndpt<=5; ndpt+=2)
                        for(int n = 100; n <=500; n=n+50){
                            //int ndpt = 5;
                            DataMD data = new DataMD(f.toString(), n+ndpt, ndpt, l);
                            ColumnGeneration_RCMDe cg = new ColumnGeneration_RCMDe(data);




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
                            Functions.writeDataCSV("Data/UCVRP"+l+"/RCMD/", data.nDepot+"-"+data.nNode+"-e", output);
                            //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                            rt.gc();

                        }
                }

//            break;
            }
        }
    }

    public static void record_pulse_md(){
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
                        DataMD data = new DataMD(f.toString(), n+ndpt, ndpt, l);
                        ColumnGeneration_PMD cg = new ColumnGeneration_PMD(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);
                            long tic = System.currentTimeMillis();
                            long toc = System.currentTimeMillis();
                            long toc2 = System.currentTimeMillis();

                            try{
                                cg.solve();
                                toc = System.currentTimeMillis();
                                cg.solveMIP();
                                toc2 = System.currentTimeMillis();
                            }catch (Exception e){

                            }

                        ArrayList<String> output = new ArrayList<>();
                        output.add(data.fileName);
                        output.add(Integer.toString(data.nNode));
                        output.add(Integer.toString(cg.nIter));
                        output.add(Integer.toString(cg.nColumns));
                        output.add(Double.toString(cg.lowerbound));
                        output.add(Double.toString(cg.upperbound));

                        //output.add(Double.toString(cg.timerBeforeLast/1000.0));
                        output.add(Double.toString((toc - tic)/1000.0));
                        output.add(Double.toString((toc2 - toc)/1000.0));

                            Functions.writeDataCSV("Data/UCVRP"+l+"/PMD/", data.nDepot+"-"+data.nNode, output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }
                }

//            break;
            }
        }
    }

    public static void record_pulse_mx(){
        {
            File file = new File("X_unit/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());

//                    if(!f.toString().endsWith("161.txt")){
//                        continue;
//                    }
                    System.out.println("here!");

                        Data data = new Data(f.toString());
                        ColumnGeneration_PMX cg = new ColumnGeneration_PMX(data);

                        //ColumnGeneration cg = new ColumnGeneration(data);

                        long tic = System.currentTimeMillis();
                        long toc1 = System.currentTimeMillis();
                        long toc2 = System.currentTimeMillis();

                        try{
                            cg.solve();
                            toc1 = System.currentTimeMillis();
                            cg.solveMIP();
                            toc2 = System.currentTimeMillis();
                        }catch (Exception e){

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
                        Functions.writeDataCSV("Data/VRPUDX/Pulse-multi/", data.fileName, output);
                        //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                        rt.gc();

                    }



        }
    }

    public static void record_randcol_mx(){
        {
            File file = new File("X_unit/");
            System.out.println(Arrays.toString(file.listFiles()));
            int counter = 0;
            for(File f : file.listFiles()) {
                System.out.println(f.toString());

//                    if(!f.toString().endsWith("161.txt")){
//                        continue;
//                    }
                System.out.println("here!");

                Data data = new Data(f.toString());
                ColumnGeneration_RCMX cg = new ColumnGeneration_RCMX(data);

                //ColumnGeneration cg = new ColumnGeneration(data);

                long tic = System.currentTimeMillis();
                long toc1 = System.currentTimeMillis();
                long toc2 = System.currentTimeMillis();

                try{
                    cg.solve();
                    toc1 = System.currentTimeMillis();
                    cg.solveMIP();
                    toc2 = System.currentTimeMillis();
                }catch (Exception e){

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
                Functions.writeDataCSV("Data/VRPUDX/RandCol-multi/", data.fileName, output);
                //Functions.writeDataCSV("Data/UCVRP4/Pulse-multi/", data.fileName+"-"+n, output);
                rt.gc();

            }



        }
    }

}

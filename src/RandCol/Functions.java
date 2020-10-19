package RandCol;

import miaoyu.helper.Route;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Functions {

    public static int pickFromProbability(int[] array){
        int max = array[array.length-1];
        double rand = Math.random();
        for(int i = 0; i < array.length; i++){
            if( ((double)array[i])/ max > rand){
                return i;
            }
        }
        return -1;
    }



    public static int randomWithDistribution(double [] prob){
        double random = Math.random();
        double[] cdf = cumulative(prob);
        for(int i = 0; i < cdf.length; i++){
            if(random <= cdf[i]){
                return i;
            }
        }
        return -1;
    }

    public static int[] cumulative(int[] array){
        int[] result = new int[array.length];
        for(int i = 0 ; i < array.length; i++){
            for(int j = 0; j <= i; j++){
                result[i] += array[j];
            }
        }
        return result;
    }

    public static double[] cumulative(double[] array){
        double[] result = new double[array.length];
        for(int i = 0 ; i < array.length; i++){
            for(int j = 0; j <= i; j++){
                result[i] += array[j];
            }
        }
        return result;
    }


    public static ArrayList<String[]> readDataCSV(String fileName){
        ArrayList<String[]> rawData = new ArrayList<String[]>();
        String csvFile = fileName;
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(csvFile));

            int counter = -1;

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);
                if(line.length() == 0){
                    continue;
                }
//                System.out.println(Arrays.toString(entry));
                rawData.add(entry);
                //System.out.println(Arrays.toString(entry));
                counter ++;
            }

        } catch (FileNotFoundException e) {
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
        return rawData;

    }

    public static void writeDataCSV(String dirName, String fileName, ArrayList<String> input){
        PrintWriter pw = null;
        try {
            File dir = new File(dirName);
            if(!dir.exists()){
                dir.mkdir();
            }
            pw = new PrintWriter(new File(dirName + fileName + ".csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        for(String str : input){
            builder.append(str);
            builder.append("\n");

        }
        pw.write(builder.toString());
        pw.close();
        System.out.println("done!");
    }

    public static String combineStringCSV(int[] input){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length-1; i++){
            sb.append(input[i]);
            sb.append(",");
        }
        sb.append(input[input.length-1]);
        return sb.toString();
    }

    // E(X)=1 ; Var(X)=1
    /** Return a random double drawn from a Gamma distribution with mean 1.0 and variance 1.0. */
    public synchronized static double nextGamma() {
        return nextGamma(1,1,0);
    }

    /** Return a random double drawn from a Gamma distribution with mean alpha and variance 1.0. */
    public synchronized static double nextGamma(double alpha) {
        return nextGamma(alpha,1,0);
    }


    /** Return a random double drawn from a Gamma distribution with mean alpha*beta and variance alpha*beta^2. */
    public synchronized static double nextGamma(double alpha, double beta) {
        return nextGamma(alpha,beta,0);
    }

    /** Return a random double drawn from a Gamma distribution with mean alpha*beta+lamba and variance alpha*beta^2. */
    public synchronized static double nextGamma(double alpha,double beta,double lambda) {
        double gamma=0;
        if (alpha <= 0 || beta <= 0) {
            throw new IllegalArgumentException ("alpha and beta must be strictly positive.");
        }
        if (alpha < 1) {
            double b,p;
            boolean flag=false;
            b=1+alpha*Math.exp(-1);
            while(!flag) {
                p=b*Math.random();
                if (p>1) {
                    gamma=-Math.log((b-p)/alpha);
                    if (Math.random()<=Math.pow(gamma,alpha-1)) flag=true;
                }
                else {
                    gamma=Math.pow(p,1/alpha);
                    if (Math.random()<=Math.exp(-gamma)) flag=true;
                }
            }
        }
        else if (alpha == 1) {
            gamma = -Math.log (Math.random());
        } else {
            double y = -Math.log (Math.random());
            while (Math.random() > Math.pow (y * Math.exp (1 - y), alpha - 1))
                y = -Math.log (Math.random());
            gamma = alpha * y;
        }
        return beta*gamma+lambda;
    }

    /*
    return the quantile of given percentage of a sorted array
    @param
     */
    public static double sortedArrayQuantile(double[] array, double percent){
        int index = (int) Math.ceil( percent * array.length );
        if(index == 0)
            return array[index];
        return array[index - 1];
    }

    public static double arrayQuantile(double[] array, double percent){
        Arrays.sort(array);
        return sortedArrayQuantile(array, percent);
    }

    public static double[] arrayQuantile(double[] array){
        double[] result = new double[7];
        Arrays.sort(array);
        result[0] = sortedArrayQuantile(array, 0.0);
        result[1] = sortedArrayQuantile(array, 0.025);
        result[2] = sortedArrayQuantile(array, 0.25);
        result[3] = sortedArrayQuantile(array, 0.5);
        result[4] = sortedArrayQuantile(array, 0.75);
        result[5] = sortedArrayQuantile(array, 0.975);
        result[6] = sortedArrayQuantile(array, 1.0);
        return result;
    }

    public static double arrayAvg(double[] array){
        double sum = 0;
        for(double ele : array){
            sum += ele;
        }
        return (sum/array.length);
    }

    public static double arrayAvg(double[] array, double num){
        double sum = 0;
        int counter = 0;
        for(double ele : array){
            if(ele > num)
                continue;
            sum += ele;
            counter += 1;
        }
        return (sum/counter);
    }

    public static int counter(double[] array, double num){
        int counter = 0;
        for(double ele : array){
            if(ele > num)
                continue;
            counter += 1;
        }
        return (counter);
    }

    public static void saveRoutes(String fileName, ArrayList<ArrayList<Route>> routes){
        ArrayList<String> output = new ArrayList<String>();
        for(int i = 0; i < routes.size(); i++){
            output.add("root,"+i);
            for(Route r : routes.get(i)){
                output.add(r.route.toString());
            }
        }
        writeDataCSV("Solution\\", fileName, output);
    }
}

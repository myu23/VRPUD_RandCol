/**
 * This class contains the parameter used for CVRPTW
 * The code is for academic use only
 * @author Miao Yu
 * @since Feb 5, 2018
 */
package miaoyu.helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DataMD {
    public int nNode = 51;
    public int nDepot = 5;
    public int dNode = 0;
    public int dDepot = 0;
    public int nVehicle;
    public int[] demand;
    public double[][][] distance;
    public double[][][] cost;
    public double[] coor_x;
    public double[] coor_y;
    public int[] tw_a;
    public int[] tw_b;
    public int[] service_time;
    public int capacity;
    public String fileName;

    public int maxThread;
    public Thread[] threads;

    public static final double R = 6372.8; // In kilometers
    public double haversine(int i, int j) {
        double lat1 = coor_x[i];
        double lon1 = coor_y[i];
        double lat2 = coor_x[j];
        double lon2 = coor_y[j];
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return (R * c)+1E-6;
        //double result = Math.floor(((R * c)+1E-6)*10)/10;
        //return result;
    }

    public DataMD(String file, int n, int nDepot, int l){
        try {
            this.nNode = n-nDepot+1;
            this.nDepot = nDepot;
            this.fileName = file.split("\\.txt")[0];
            FileReader inputFile = new FileReader(file);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            String[] entry;
            int i, j;

            System.out.println("Reading Data Start");
            //read header

            line = bufferReader.readLine();
            this.dDepot = Integer.parseInt(line.trim().split(",")[1]);
            line = bufferReader.readLine();
            this.dNode = Integer.parseInt(line.trim().split(",")[1]);
            bufferReader.readLine();

            //this.nVehicle = Integer.parseInt(entry[0]);
            //initialize parameters
            this.coor_x = new double[dNode+dDepot];
            this.coor_y = new double[dNode+dDepot];
            this.demand = new int[dNode+dDepot+1];
            this.tw_a = new int[dNode+dDepot+1];
            this.tw_b = new int[dNode+dDepot+1];
            this.service_time = new int[dNode+dDepot+1];
            this.capacity = l;//Integer.parseInt(entry[1]);

            //read depots
            for(i = 0; i < dDepot; i++){
                line = bufferReader.readLine();
                entry = line.trim().split(",");
                this.coor_x[i] = Double.parseDouble(entry[8]);
                this.coor_y[i] = Double.parseDouble(entry[9]);
                this.demand[i] = 0;//Integer.parseInt(entry[3]);
                this.tw_a[i] = 0;//Integer.parseInt(entry[4]);

                this.tw_b[i] = 1000;//Integer.parseInt(entry[5]);
                this.service_time[i] = 0;//Integer.parseInt(entry[6]);

            }
            //read customer
            for(i = dDepot; i < dNode+dDepot; i++){
                line = bufferReader.readLine();
                entry = line.trim().split(",");
                this.coor_x[i] = Double.parseDouble(entry[8]);
                this.coor_y[i] = Double.parseDouble(entry[9]);
                this.demand[i] = 1;//Integer.parseInt(entry[3]);
                this.tw_a[i] = 0;//Integer.parseInt(entry[4]);
                this.tw_b[i] = 1000;//Integer.parseInt(entry[5]);
                this.service_time[i] = 0;//Integer.parseInt(entry[6]);
            }



            this.tw_a[dNode+dDepot] = this.tw_a[0];
            this.tw_b[dNode+dDepot] = this.tw_b[0];
            this.demand[dNode+dDepot] = this.demand[0];
            this.service_time[dNode+dDepot] = 0;

            this.distance = new double[nDepot][nNode+1][nNode+1];
            this.cost = new double[nDepot][nNode+1][nNode+1];
            for(int k = 0; k < nDepot; k++) {
                for(j = 1; j < nNode; j++){
                    this.distance[k][0][j] = haversine(k, j+dDepot-1);
                    this.distance[k][j][0] = this.distance[k][0][j];
                }
                for (i = 1; i < nNode; i++) {
                    for (j = i; j < nNode; j++) {
                        this.distance[k][i][j] = haversine(i+dDepot-1, j+dDepot-1);
                        this.distance[k][j][i] = this.distance[k][i][j];
                    }
                    this.distance[k][i][nNode] = haversine(i+dDepot-1, k);
                    this.distance[k][nNode][i] = this.distance[k][i][nNode];
                }
            }


            System.out.println("Reading complete");
            bufferReader.close();
        }catch(IOException e){
            System.out.println("Error while reading file line by line: " + e.getMessage());
        }
    }


    public void setThread(int n){
        this.maxThread = n;
        this.threads = new Thread[maxThread+1];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread();
        }
    }

//    public double compute_dist(int i , int j){
//        return Math.floor((Math.sqrt(Math.pow(coor_x[i]-coor_x[j], 2) + Math.pow(coor_y[i] - coor_y[j], 2))*10))/10;
//    }


    public static void main(String[] args){
        //for test purpose
        DataMD data = new DataMD("./WayneData/Wayne5.csv", 55, 5,4);
        System.out.println(data.fileName);
        System.out.println("Test data");
        System.out.println("n, k, capacity: "+ data.nNode + " " + data.nVehicle + " "+data.capacity);
        System.out.println(Arrays.toString(data.coor_x));
        System.out.println(Arrays.toString(data.service_time));
//        System.out.println("check triangle ineq");
//        for(int d = 0; d < data.nDepot; d++) {
//            for (int i = 0; i < data.nNode + 1; i++) {
//                for (int j = 0; j < data.nNode + 1; j++) {
//                    for (int k = 0; k < data.nNode + 1; k++) {
//                        double delta = data.distance[d][i][k] + data.distance[d][k][j] - data.distance[d][i][j];
//                        if (delta < 0) {
//                            System.out.println(i + " " + k + " " + j + ":" + delta);
////                            System.out.println(i + ": " + data.coor_x[i+data.nNode] + "," + data.coor_y[i+data.nNode]);
////                            System.out.println(j + ": " + data.coor_x[j+data.nNode] + "," + data.coor_y[j+data.nNode]);
////                            System.out.println(k + ": " + data.coor_x[k+data.nNode] + "," + data.coor_y[k+data.nNode]);
//
//                            System.out.println(d+":"+data.distance[d][i][k] + "("+i+","+k+") " + data.distance[d][k][j] + " " + data.distance[d][i][j]);
//                        }
//
//                    }
//                }
//            }
//        }
        for(int i=0; i < data.nNode; i++)
            System.out.println(Arrays.toString(data.distance[0][i]));
        System.out.println("done");
    }
}

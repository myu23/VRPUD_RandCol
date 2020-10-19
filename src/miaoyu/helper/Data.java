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
import java.util.*;

public class Data {
    public int nNode = 51;
    public int nVehicle;
    public int[] demand;
    public double[][] distance;
    public double[][] cost;
    public int[] coor_x;
    public int[] coor_y;
    public int[] tw_a;
    public int[] tw_b;
    public int[] service_time;
    public int capacity;
    public String fileName;

    public int maxThread;
    public Thread[] threads;


    public Data(String file, int n, int l){
        try {
            this.nNode = n;
            this.fileName = file.split("\\.txt")[0];
            FileReader inputFile = new FileReader(file);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            String[] entry;
            int i, j;

            System.out.println("Reading Data Start");
            //read redundant lines
            for (i = 0; i < 4; i++) {
                bufferReader.readLine();
            }

            //read nNode and nVehicle
            line = bufferReader.readLine();
            entry = line.trim().split("\\s+");
            this.nVehicle = Integer.parseInt(entry[0]);
            this.capacity = l;//Integer.parseInt(entry[1]);

            //initialize parameters
            this.coor_x = new int[nNode];
            this.coor_y = new int[nNode];
            this.demand = new int[nNode+1];
            this.tw_a = new int[nNode+1];
            this.tw_b = new int[nNode+1];
            this.service_time = new int[nNode+1];
            //read node details
            for(i = 0; i < 4; i++)
                bufferReader.readLine();

            for(i = 0; i < nNode; i++){
                line = bufferReader.readLine();
                entry = line.trim().split("\\s+");
                this.coor_x[i] = Integer.parseInt(entry[1]);
                this.coor_y[i] = Integer.parseInt(entry[2]);
                this.demand[i] = 1;//Integer.parseInt(entry[3]);
                this.tw_a[i] = 0;//Integer.parseInt(entry[4]);
                if(i == 0){
                    this.tw_b[i] = Integer.parseInt(entry[5]);
                    this.demand[i] = Integer.parseInt(entry[3]);

                }
                this.tw_b[i] = this.tw_b[0];//Integer.parseInt(entry[5]);
                //this.tw_a[i] = Integer.parseInt(entry[4]);

                //this.tw_b[i] = Integer.parseInt(entry[5]);
                this.service_time[i] = 0;//Integer.parseInt(entry[6]);
            }
            this.tw_a[nNode] = this.tw_a[0];
            this.tw_b[nNode] = this.tw_b[0];
            this.demand[nNode] = this.demand[0];
            this.service_time[nNode] = 0;//= this.demand[0];

            this.distance = new double[nNode+1][nNode+1];
            this.cost = new double[nNode+1][nNode+1];
            for(i = 0; i < nNode; i++){
                for(j = i; j < nNode; j++){
                    this.distance[i][j] = compute_dist(i,j);
                    this.distance[j][i] = this.distance[i][j];
                }
                this.distance[i][nNode] = compute_dist(i,0);
                this.distance[nNode][i] = this.distance[i][nNode];
            }
            System.out.println("Reading complete");
            bufferReader.close();
        }catch(IOException e){
            System.out.println("Error while reading file line by line: " + e.getMessage());
        }
    }

    public Data(String file, int L){
        try {
            this.fileName = file.split("\\.vrp")[0];
            FileReader inputFile = new FileReader(file);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            String[] entry;
            int i, j;

            System.out.println("Reading Data Start");
            //read redundant lines
            for (i = 0; i < 7; i++) {
                line = bufferReader.readLine();
                entry = line.trim().split("\\s+");
                if(entry[0].startsWith("DIMENSION")){
                    this.nNode = Integer.parseInt(entry[entry.length-1]);
                }else if(entry[0].startsWith("CAPACITY")){
                    this.capacity = L;//Integer.parseInt(entry[entry.length-1]);
                }
            }

            //read nNode and nVehicle



            //initialize parameters
            this.coor_x = new int[nNode];
            this.coor_y = new int[nNode];
            this.demand = new int[nNode+1];
            this.tw_a = new int[nNode+1];
            this.tw_b = new int[nNode+1];
            this.service_time = new int[nNode+1];

            for(i = 0; i < nNode; i++){
                line = bufferReader.readLine();
                entry = line.trim().split("\\s+");
                this.coor_x[i] = Integer.parseInt(entry[1]);
                this.coor_y[i] = Integer.parseInt(entry[2]);
                this.demand[i] = 1;//Integer.parseInt(entry[3]);
                this.tw_a[i] = 0;//Integer.parseInt(entry[4]);
                if(i == 0){
                    this.tw_b[i] = 5000;
                    this.demand[i] = 0;

                }
                this.tw_b[i] = this.tw_b[0];//Integer.parseInt(entry[5]);
                //this.tw_a[i] = Integer.parseInt(entry[4]);

                //this.tw_b[i] = Integer.parseInt(entry[5]);
                this.service_time[i] = 0;//Integer.parseInt(entry[6]);
            }
            this.tw_a[nNode] = this.tw_a[0];
            this.tw_b[nNode] = this.tw_b[0];
            this.demand[nNode] = this.demand[0];
            this.service_time[nNode] = 0;//= this.demand[0];

            this.distance = new double[nNode+1][nNode+1];
            this.cost = new double[nNode+1][nNode+1];
            for(i = 0; i < nNode; i++){
                for(j = i; j < nNode; j++){
                    this.distance[i][j] = compute_dist_i(i,j);
                    this.distance[j][i] = this.distance[i][j];
                }
                this.distance[i][nNode] = compute_dist_i(i,0);
                this.distance[nNode][i] = this.distance[i][nNode];
            }
            System.out.println("Reading complete");
            bufferReader.close();
        }catch(IOException e){
            System.out.println("Error while reading file line by line: " + e.getMessage());
        }
    }


    public Data(String file){
        try {
            this.fileName = file.split("\\.vrp")[0];
            FileReader inputFile = new FileReader(file);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            String[] entry;
            int i, j;

            System.out.println("Reading Data Start");
            //read redundant lines
            for (i = 0; i < 7; i++) {
                line = bufferReader.readLine();
                entry = line.trim().split("\\s+");
                if(entry[0].startsWith("DIMENSION")){
                    this.nNode = Integer.parseInt(entry[entry.length-1]);
                }else if(entry[0].startsWith("CAPACITY")){
                    this.capacity = Integer.parseInt(entry[entry.length-1]);
                }
            }

            //read nNode and nVehicle



            //initialize parameters
            this.coor_x = new int[nNode];
            this.coor_y = new int[nNode];
            this.demand = new int[nNode+1];
            this.tw_a = new int[nNode+1];
            this.tw_b = new int[nNode+1];
            this.service_time = new int[nNode+1];

            for(i = 0; i < nNode; i++){
                line = bufferReader.readLine();
                entry = line.trim().split("\\s+");
                this.coor_x[i] = Integer.parseInt(entry[1]);
                this.coor_y[i] = Integer.parseInt(entry[2]);
                this.demand[i] = 1;//Integer.parseInt(entry[3]);
                this.tw_a[i] = 0;//Integer.parseInt(entry[4]);
                if(i == 0){
                    this.tw_b[i] = 5000;
                    this.demand[i] = 0;

                }
                this.tw_b[i] = this.tw_b[0];//Integer.parseInt(entry[5]);
                //this.tw_a[i] = Integer.parseInt(entry[4]);

                //this.tw_b[i] = Integer.parseInt(entry[5]);
                this.service_time[i] = 0;//Integer.parseInt(entry[6]);
            }
            this.tw_a[nNode] = this.tw_a[0];
            this.tw_b[nNode] = this.tw_b[0];
            this.demand[nNode] = this.demand[0];
            this.service_time[nNode] = 0;//= this.demand[0];

            this.distance = new double[nNode+1][nNode+1];
            this.cost = new double[nNode+1][nNode+1];
            for(i = 0; i < nNode; i++){
                for(j = i; j < nNode; j++){
                    this.distance[i][j] = compute_dist_i(i,j);
                    this.distance[j][i] = this.distance[i][j];
                }
                this.distance[i][nNode] = compute_dist_i(i,0);
                this.distance[nNode][i] = this.distance[i][nNode];
            }
            System.out.println("Reading complete");
            bufferReader.close();
        }catch(IOException e){
            System.out.println("Error while reading file line by line: " + e.getMessage());
        }
    }

    public double compute_dist_i(int i , int j){
        return Math.floor(Math.sqrt(Math.pow(coor_x[i]-coor_x[j], 2) + Math.pow(coor_y[i] - coor_y[j], 2))+0.5);
    }

    public void setThread(int n){
        this.maxThread = n;
        this.threads = new Thread[maxThread+1];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread();
        }
    }

    public double compute_dist(int i , int j){
        return Math.floor((Math.sqrt(Math.pow(coor_x[i]-coor_x[j], 2) + Math.pow(coor_y[i] - coor_y[j], 2))*10))/10;
    }


    public static void main(String[] args){
        //for test purpose
        Data data = new Data("./solomon_100/C103.txt", 50, 4);
        System.out.println(data.fileName);
        System.out.println("Test data");
        System.out.println("n, k, capacity: "+ data.nNode + " " + data.nVehicle + " "+data.capacity);
        System.out.println(Arrays.toString(data.coor_x));
        System.out.println(Arrays.toString(data.service_time));
        System.out.println("check triangle ineq");
        for(int i = 0; i < data.nNode+1; i++){
            for (int j = 0; j < data.nNode+1; j++){
                for(int k = 0; k < data.nNode+1; k++){
                    double delta = data.distance[i][k] + data.distance[k][j] - data.distance[i][j];
                    if(delta< 0){
                        System.out.println(i+" "+k+" "+j+":"+delta);
                        System.out.println(i+ ": "+data.coor_x[i]+","+data.coor_y[i]);
                        System.out.println(j+ ": "+data.coor_x[j]+","+data.coor_y[j]);
                        System.out.println(k+ ": "+data.coor_x[k]+","+data.coor_y[k]);

                        System.out.println(data.distance[i][k] +" " +data.distance[k][j] +" "+ data.distance[i][j]);
                    }

                }
            }
        }
        System.out.println("done");
    }
}

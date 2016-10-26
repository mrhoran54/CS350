// Simulator for MM1
// Author: Megan Horan (mrhoran@bu.edu)
// 10 / 9 /16
// 

import java.util.*;
import java.util.Random;
import java.io.*;
import java.io.PrintWriter;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;


// 0 for in the disk
// 1 for in the network
// 2 if in the cpu queue

public class Controller {
    
    // the input values, different for each simulator
    public static double LAMBDA;
    public static double TS;
    public static double SIMULATION_TIME;
    
    // status of the server
    public static final int BUSY = 1;
    public static final int IDLE = 0;
    
    public static int Server;
    
    MinHeap2 schedule;     // storing the schedule of events for the system
    
    LinkedList<Request> buffer;   // this will keep track of actual events in the queue
    
    // some stats to keep track of 
    double Tq ;
    double Tw;
    double Ts;
    
    double open_serverCount;
    double w;
    double q;
        
    double time;
   
    int requests;
    int serviced;
    int num_of_monitors;
    
   
    // create a controller object
   public Controller(double lambda, double ts, double simulation_time) {
        
        this.LAMBDA = lambda;
        this.TS = ts;
        this.SIMULATION_TIME = simulation_time;
       
        time = 0; //intitial time is zero
        
        // create a new scedule and initialize it with a birth birth event
        
        schedule = new MinHeap2();
        
        schedule.insert(new CalenderEvent("Birth", 0));
        schedule.insert(new CalenderEvent("Monitor", SIMULATION_TIME));
             
        buffer = new LinkedList<Request>();
        
        Server = 0;
        
        Tq = 0; 
        Tw = 0;
        Ts = 0;
        
        requests = 1;
        serviced = 0 ;
        num_of_monitors= 1;
   
        w= 0; 
        q = 0;
       
    }

    // exponential function to get the next arrival times
    //given by a random variable mapped to an exponential distribution
    
    public static double exponential(double val) {
        
        Random r = new Random();
        
        double x = (Math.log(1-r.nextDouble())/(-val));
        
        return x;
    }
    
    // what to do if there is a birth
    
    public void birthEvent(double time, double ts, double lambda){
                     
        // if the queue is empty put it in the cpu queue!
        if (buffer.size() == 0) {
            
            double serviceTime = exponential(1/ts); 
            
            // generate the death time 
            // since there is no wait, arrival time and time it entered the queue is the same
            
            buffer.add(new Request(requests, time, time, 0.0));
            
            // we know the time a death will occur by generating a random service time
            
            schedule.insert(new CalenderEvent("Death", time + serviceTime));
            
        }
         // if the queue is not empty, add it!
        
        else{
            
            // we dont know the time it will leave the buffer or leave the system at this point so
            // we set those fields equal to zero
            
            buffer.add(new Request(requests, time, 0.0 , 0.0));
         
        }
        
        // finally, schedule the next arrival time 
        
        double next_arrival_time = exponential(lambda);
        
        schedule.insert(new CalenderEvent("Birth", time + next_arrival_time));
        
    }
    
    // what to do if there is a death
    public void deathEvent(double time, double ts){
                        
        // pop that event from the queue if the server is not busy
        Request remove = (buffer.removeFirst());

        Tw += (remove.leftQueue - remove.arrivalTime);
        Tq += ((remove.leftQueue - remove.arrivalTime) + (time - remove.leftQueue)); 
        
        open_serverCount++;
       
        double serviceTime1 = exponential(1/ts);
        double serviceTime2 = exponential(1/ts);
        
       if (buffer.size() != 0){
       
           
            
           double serviceTime = exponential(1/ts);
        
           schedule.insert(new CalenderEvent("Death", time + serviceTime));
          
           //update the next event's leaving of queue and starting service time
           buffer.getFirst().leftQueue = time;
           
           Server = BUSY;
           
           // get the service times
           Ts += serviceTime;
       }
       
       else{
           // if the queue is empty, dont schedule a death ie Server is idle
           
           Server = IDLE;
           
       }   
    }

    // monitoring event!!
    
     public void monitorEvent(double time, double lambda){
        
         double current_q = 0;
           
             // get w ie size of buffer
         if (buffer.size() == 0)
             
             current_q = 0;
         
         else 
              current_q = buffer.size()-1;
         
          double current_w = 0;
         
         if (Server == BUSY)
           
             current_w = (buffer.size() - 2);
         
         else if(Server == IDLE)
           
             current_w = 0;
         
         w += current_w; 
         q += current_q;
 
      // we want to schedule the next arrival time based on an exp distribution 
       
       double arrival_time = exponential(lambda/2);
        
       schedule.insert(new CalenderEvent("Monitor", time + arrival_time));
      
    }
      
    public void printResults(PrintWriter out) {
        
        System.out.println("\nResults of this MM1 Simulator:");
        
        System.out.println("w = " + w/num_of_monitors);
        System.out.println("q = " + q/num_of_monitors);
        
        System.out.println("Tw = " + Tw/requests);
        
        System.out.println("Tq = " + Tq/serviced);
         
        System.out.println("server utilization = " + (q/num_of_monitors - w/num_of_monitors));
        
        
         out.println("w = " + w/num_of_monitors);
         out.println("q = " + q/num_of_monitors);
        
         out.println("Tw = " + Tw/requests);
        
         out.println("Tq = " + Tq/serviced);
         
         out.println("server utilization = " + (q/num_of_monitors - w/num_of_monitors));
       
         
         
       
    }
    
    public void runMM1(PrintWriter out) { 
        
       
       while(time < (2*SIMULATION_TIME)){
            
            CalenderEvent newEvent = (schedule.getMin());
            
            time = newEvent.time;
           
            
            if(newEvent.id == "Birth"){
       
                birthEvent(time, TS, LAMBDA);
                out.println("Birth event at " + time);
                birthEvent(time, TS, LAMBDA);
                requests++;
    
            }
             else if(newEvent.id == "Death"){
                
                 out.println("Death event at " + time);
                 deathEvent(time, TS);
                 serviced++;
            }
            else if(newEvent.id == "Monitor"){
                
                out.println("Monitor event at " + time);
                monitorEvent(time, LAMBDA);
                num_of_monitors++;
               
            }

       }
       
 
     out.close();
     
    }
    
    public static void main(String[] args) { 
        
         File dir = new File("logs");
        if (!dir.exists()) {
         dir.mkdir();
        }
        
        File dir2 = new File("logs/mm1");
        if (!dir2.exists()) {
         dir2.mkdir();
        }
        
        
        //uncomment this block to get the result of a
        File file1 = new File("logs/mm1/sim1_stat_log.txt"); 
         
        //uncomment this block to get the result of a
        File file2 = new File("logs/mm1/sim2_stat_log.txt"); 
        
         //uncomment this block to get the result of a
        File file3 = new File("logs/mm1/sim3_stat_log.txt"); 
        
        PrintWriter out;
        
        double lambda = 5.0;
        double ts = 0.15;
        double length = .1;
        
        try {
            
            PrintWriter out1 = new PrintWriter(file1);
  
        
            Controller c1 = new Controller(lambda,ts,length);
            
            c1.runMM1(out1);
            
            c1.printResults(out1);
           
            
            PrintWriter out2 = new PrintWriter(file2);
      
             // lambda = 6.0 ts = .15 legnth time = 10000
            Controller c2 = new Controller(6.0,ts,1000);
            
            c2.runMM1(out2);
            
            c2.printResults(out2);
            
            PrintWriter out3 = new PrintWriter(file3);
      

        // lambda = 6.0 ts = .2 legnth time = 10000
            Controller c3 = new Controller(6.0, 0.2 ,length);
            
           
            c3.runMM1(out3);
            c3.printResults(out3);
      
        
    }
        
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    
    } 
}
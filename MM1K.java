// Simulator for MM1
// Author: Megan Horan (mrhoran@bu.edu)
// 10 / 9 /16
// 

import java.util.*;
import java.util.Random;
import java.io.*;


public class MM1K {
    
    // the input values, different for each simulator
    public static double LAMBDA;
    public static double TS;
    public static double SIMULATION_TIME;
    public static double K;
    
    // status of the server
    public static final int BUSY = 1;
    public static final int IDLE = 0;
    
    public static int Server;
    
    double currentTime;// keeps track of the time thoughout the system
      
    MinHeap2 schedule;     // storing the schedule of events for the system
    
    LinkedList<Request> buffer;   // this will keep track of actual events in the queue
    
    // some stats to keep track of 
    double Tq ;
    double Ts;
        
    double time;
   
    int requests;
    int serviced ;
    int num_of_monitors;
    
    //double K;
    
    double rejected;
    double q;
    double w;
    
   
    // create a controller object
   public MM1K(double k, double lambda, double ts, double simulation_time) {
        
        this.LAMBDA = lambda;
        this.TS = ts;
        this.SIMULATION_TIME = simulation_time;
        this.K = k;
       
        time = 0; //intitial time is zero
        
        // create a new scedule and initialize it with a birth birth event
        
        schedule = new MinHeap2();
        
        schedule.insert(new CalenderEvent("Birth", 0));
        schedule.insert(new CalenderEvent("Monitor", SIMULATION_TIME));
        
                    
        buffer = new LinkedList<Request>();
        
        Server = 0;
        
        Tq = 0; 
        Ts = 0;
       
        requests = 1;
        serviced = 0 ;
        num_of_monitors= 1;
   
        rejected = 0; 
        q = 0;
        w = 0;
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
                     
        // if the queue is empty 
        if (buffer.size() == 0) {
           
            
            double serviceTime = exponential(1/ts);
            
            Ts += serviceTime;
            
            // generate the death time 
            // since there is no wait, arrival time and time it entered the queue is the same
            
            buffer.add(new Request(requests, time, time, 0.0));
            
            // we know the time a death will occur by generating a random service time
            
            schedule.insert(new CalenderEvent("Death", time + serviceTime));
             
        }
         // if the queue has k elements in it, then put it in the buffer
        
        else if(buffer.size() <= K){
            
            // we dont know the time it will leave the buffer or leave the system at this point so
            // we set those fields equal to zero
            
            buffer.add(new Request(requests, time, 0.0 , 0.0));
         
        }
        // if its full, add it to the running tally of those rejected
        else
        
            rejected++;
        
        // finally, schedule the next arrival time 
        
        double next_arrival_time = exponential(lambda);
        
        schedule.insert(new CalenderEvent("Birth", time + next_arrival_time));
        
    }
    
    // what to do if there is a death
    public void deathEvent(double time, double ts){
                        
        // pop that event from the queue if the server is not busy
        Request remove = (buffer.removeFirst());

        Tq += ((remove.leftQueue - remove.arrivalTime) + (time - remove.leftQueue)); 
        
       
       // if the queue is not empty, set Server = busy and schedule the next death event
            
       if (buffer.size() != 0){
           
           // generate a random service time according to the dist of random service time
           double serviceTime = exponential(1/ts);
        
           schedule.insert(new CalenderEvent("Death", time + serviceTime));
           
           //update the next event's leaving of queue and starting service time
           buffer.getFirst().leftQueue = time;
           
           Server = BUSY;
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
              current_q = buffer.size();
         
         double current_w = 0;
         
         if (Server == BUSY)
           
             current_w = (buffer.size() - 2);
         
         else if(Server == IDLE)
           
             current_w = 0;
       
         //ad those to the averages
         w += current_w; 

         q += current_q;
 
      // we want to schedule the next arrival time based on an exp distribution 
       
       double arrival_time = exponential(lambda/2);
        
       schedule.insert(new CalenderEvent("Monitor", time + arrival_time));
      
    }
      
    public void printResults(double lambda) {
        
        System.out.println("\nResults of this MM1K Simulator:");
        
        System.out.println("\nLambda  = " + lambda + ",TS = " + TS + ", K = " + K +"\n");
        
        double probRejection =  rejected/requests;
        
        System.out.println("prob of rejection = " + probRejection);
                           
        System.out.println("q = " +  q / num_of_monitors);
        
        System.out.println("server utilization = " + (q/num_of_monitors - w/num_of_monitors));
       
        System.out.println("Tq = " + Tq/serviced);
        
        
    }
    
    public void runMM1() { 
        
       
       while(time < (2*SIMULATION_TIME)){
            
            CalenderEvent newEvent = (schedule.getMin());
            
            time = newEvent.time;
            
            
            if(newEvent.id == "Birth"){
       
                //System.out.println("Birth event" + time);
                birthEvent(time, TS, LAMBDA);
                requests++;
    
            }
             else if(newEvent.id == "Death"){
                
                 //System.out.println("Death event" + time );
                 deathEvent(time, TS);
                 serviced++;
            }
            else if(newEvent.id == "Monitor"){
                
                //System.out.println("Monitor event" + time);
                monitorEvent(time, LAMBDA);
                num_of_monitors++;
               
            }

       }
      
    }
    
    public static void main(String[] args) { 
        
        double lambda = 6.0;
        double ts = 0.15;
        double length = 100; 
        double k = 10;
      
        MM1K c1 = new MM1K(k, lambda,ts,length);
        
        c1.runMM1();
        
        c1.printResults(lambda);

        
    }
    
  
}
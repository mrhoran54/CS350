// Simulator for queue in homework 4
// Author: Megan Horan (mrhoran@bu.edu)
//
// 

import java.util.*;
import java.util.Random;
import java.io.*;

public class NetworkofQueues {
    
    // the input values, different for each simulator
    
    public static double LAMBDA;
    public static double TS_CPU;
    public static double TS_DISK;
    public static double TS_NETWORK;
    public static double SIMULATION_TIME;
    
    // status of the server
    public static final int BUSY = 1;
    public static final int IDLE = 0;
    
    public static final int OLD_BIRTH = 1;
    public static final int NEW_BIRTH = 0;
    
    int Server1;
    int Server2;
    
    int disk_server;
    int network_server;
    
     MinHeap2 schedule;     // storing the schedule of events for the system
    
    LinkedList<Request2> cpu_buffer;   // this will keep track of actual events in the queue
    
    LinkedList<Request2> network_buffer;
    
    LinkedList<Request2> disk_buffer;
    
    // some stats to keep track of 
    double Tq ;
   
    double time;
   
    int serviced;
    int requests;
    int num_of_monitors;
    int num_births;
    
     double q;
     
     double q_cpu;
    
     double Ts_cpu ;
     double Ts_disk ;
     double Ts_network;
     
     double Tw_disk;
     double Tw_network;
     double Tw_cpu;
     
     int num_death_resquests;
     int num_network_requests;
     int num_network_deaths;
     int num_disk_requests;
     int num_disk_deaths;
     
     double w_cpu;
     double w_cpu_average;
     double q_disk;
     double q_net;
    
   public NetworkofQueues(double lambda, double ts_cpu, double ts_disk,double ts_network, double simulation_time) {
        
        this.LAMBDA = lambda;
        this.TS_CPU = ts_cpu;
        this.TS_DISK = ts_disk;
        this.TS_NETWORK = ts_network;
        this.SIMULATION_TIME = simulation_time;
       
        time = 0; //intitial time is zero
        
        // create a new scedule and initialize it with a birth birth event
        
        schedule = new MinHeap2();
        
        schedule.insert(new CalenderEvent2("Birth",0, 0));
        
        schedule.insert(new CalenderEvent2("Monitor",0, 0));
        
        cpu_buffer = new LinkedList<Request2>();
        
        disk_buffer = new LinkedList<Request2>();
        
        network_buffer = new LinkedList<Request2>();
    
        //open_serverCount = 2; // start off with two open servers
        
        Tq = 0; 
        
        Ts_cpu = 0;
        Ts_disk = 0;
        Ts_network = 0;

        q= 0;
        
        // set up functionality for the dual core
        Server1 = IDLE;
        Server2 =  IDLE;
        
        w_cpu = 0;
        q_disk = 0;
        q_net = 0;
        q_cpu = 0;
        
        Tw_disk = 0;
        Tw_network = 0;
        Tw_cpu = 0;
         
        num_network_deaths = 0;
        num_network_requests = 0; 
        num_death_resquests = 0; // for  calc ts cpu
        
        num_disk_requests = 0; 
        num_disk_deaths = 0;
       
        requests = 0;
        serviced = 0;
        num_of_monitors = 0;
        num_births = 0;
    }

    // exponential function to get the next arrival times
    //given by a random variable mapped to an exponential distribution
    
    public static double exponential(double val) {
        
        Random r = new Random();
        
        double x = (Math.log(1-r.nextDouble())/(-val));
        
        return x;
    }
    // some code I used for problem 3 !
//    public static double normalDist() {
//  
//        double mean = .1;
//        double stdDev = 0.03;
//        
//        Random rand = new Random();
//        
//        double nRV = rand.nextGaussian(); // normalRV from N(0,1)
//  
//       
////  
//        while(nRV < 0){
////             
//            nRV = stdDev*nRV+mean; // linear trans to (mean,std dev)
//            normalDist();
////      
//        }
//        return nRV;
//// 
//    }
//    public static double uniformDist() {
//  
//        double low = 0.001;
//        double high = 0.039;
//        
//        Random rand = new Random();
//        
//        double res = rand.nextDouble();
//  
//        res = ((high-low)*res)+low;
//  
//        return res;
//    }
//    
    // birth from the outside
     public void birthEvent(double time){
      
        // if the queue is empty you can predict immediately what will happen
        if (cpu_buffer.size() == 0 && Server2 == IDLE && Server1 == IDLE) {
            
            cpu_buffer.add(new Request2(NEW_BIRTH, time, time, 0.0, 0.0, 0.0)); // add to the cpu buffer
            
            // since the buffer is empty we know we can call the death event!
            double serviceTime = exponential(1/TS_CPU); 
            
            Ts_cpu += serviceTime; 
           
                if(Server1 == IDLE && Server2 == IDLE){
                    
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
                }
                else if(Server1 == BUSY && Server2 == IDLE){
                
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",2, time + serviceTime));
                    Server2 = BUSY;
                
                }
                else if(Server2 == BUSY && Server1 == IDLE){
                 
                    Ts_cpu += serviceTime;
                
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
                }
               
           
        }
        
        else{ // if the buffer isnt empty, just add it to the cpu_queue
           
            cpu_buffer.add(new Request2(NEW_BIRTH, time, 0.0, 0.0, 0.0, 0.0)); 
            
        }
        
        // finally, schedule the next arrival time! 
        
        double next_arrival_time = exponential(LAMBDA);
        
        schedule.insert(new CalenderEvent2("Birth",0, time + next_arrival_time));
        
    }
     
     // what to do if there is a death from cpu
    public void deathEvent(double time, int core_cpu){
          
        // keep updating w_cpu_average for stats at the end
       w_cpu_average += cpu_buffer.size(); 
       
      // generate a random number to see what to do next
       double xx = Math.random();
       
       Request2 remove = (cpu_buffer.removeFirst());  
       int id = remove.id;
       double arrival_time = remove.arrivalTime; // keep arrival time consistant for the requests
       double left_queue = remove.left_queue;
     
       // set the appropriate cores to get ready to service the next death
       if(core_cpu == 1){
           
           Server1 = IDLE;
       }
       
       else if(core_cpu == 2){
           
           Server2 = IDLE;
       }
       
       if(xx > .5){
           
           serviced++;
           
           Tq += time - arrival_time; // if it was an actual " death", see how long it was in the system
       }
       
       else if(xx <= .1){  // else enters the disk
           
           num_disk_requests++;
           disk_birth(time, id, arrival_time, left_queue);
       }
       
       else if(0.1 < xx && xx < .5) {// else enter the network and call a function to put it in the network
           
           num_network_requests++;
           network_birth(time, id, arrival_time, left_queue);
       }
       
       double serviceTime1 = exponential(1/TS_CPU); ;

       double serviceTime2 = exponential(1/TS_CPU); 
       
       // dual core functionality
       
       if(cpu_buffer.size() != 0 && Server2 == IDLE && Server1 == IDLE){ // if there is more room in the queue and avalible servers, schedule more  
           
           
           if(cpu_buffer.size() >= 2 && Server1 == IDLE && Server2 == IDLE){// if you know there are at least 2 elements in buffer schedule 2 deaths
                
                   Ts_cpu += serviceTime1;
                   Ts_cpu += serviceTime2;
          
                   schedule.insert(new CalenderEvent2("Death",1, time + serviceTime1));
                   schedule.insert(new CalenderEvent2("Death",2, time + serviceTime2));
                   Server1 = BUSY; Server2 = BUSY;

           }

           else if(Server1 == IDLE && Server2 == IDLE && cpu_buffer.size() < 2){ // if both are idle but only one item in queue, sche 1 death
               
                   Ts_cpu += serviceTime1;
                 
                   schedule.insert(new CalenderEvent2("Death",1, time + serviceTime1));
                   Server1 = BUSY;
                   
           }
           
           else if((Server1 - Server2) == 1 || (Server1 - Server2) == -1){ // if one is idle and other isnt make the idle core busy
             
              
               if(Server1 == IDLE){
               
                   Ts_cpu += serviceTime1;
                   schedule.insert(new CalenderEvent2("Death",1, time + serviceTime1));

                   Server1 = BUSY;
                   
               }
               else{
                   Ts_cpu += serviceTime1;
                   schedule.insert(new CalenderEvent2("Death",2, time + serviceTime1));

                   Server2 = BUSY;
                   
               }
               }
           }
       
    }
    
    public void disk_birth(double time, int id, double arrival_time, double left_queue){
       
        num_disk_requests++;
        
        if (disk_buffer.size() == 0) { //if the disk buffer is empty schedule a disk_death
 
            double serviceTime = exponential(1/TS_DISK);
  
            Ts_disk += serviceTime;
            
            disk_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, time + serviceTime, 0.0, 0.0)); 
            
            schedule.insert(new CalenderEvent2("disk_death",0, time + serviceTime));
            
        }
        else{ // else just add it to the disk buffer!
            
            disk_buffer.add(new Request2(id, arrival_time, left_queue, 0.0, 0.0, 0.0)); // add it to the universe queue
        
            
        }
    }
    
    // what to do if there is a network birth 
    
     public void network_birth(double time, int id, double arrival_time, double left_queue){
        
        num_network_requests++;
        
        if (network_buffer.size() == 0) { // if the buffer is empty add it to the network queue and then service it
            
            double serviceTime = exponential(1/TS_NETWORK);
            
            Ts_network += serviceTime;
            
            network_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, 0.0, time + serviceTime, 0.0)); 
            
            schedule.insert(new CalenderEvent2("network_death",0, time + serviceTime));
            
        }
        else // if the buffer isnt empty, just keep it in there
            network_buffer.add(new Request2(id, arrival_time, left_queue, 0.0, 0.0,0.0)); 
    
    }

     // what to do when there is a disk death
        
    public void disk_death(double time){

        
        // pop it off the disk_buffer
        num_disk_deaths++;
        
        Request2 remove = disk_buffer.removeFirst();
        
        double arrival_time = remove.arrivalTime;
        double left_queue = remove.left_queue;
        
        disk_server = IDLE;
        
        Tw_disk += (time - left_queue); // see how long it was in the disk
            
        double xx = Math.random();
        
        if(xx >= .5){ // .5 probability you will actually enter the cpu queue
              
            if (cpu_buffer.size() == 0 && Server2 == IDLE && Server1 == IDLE) { // either the cpu queue is empty and so we are going to add it to the buffer
           
                
                cpu_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, 0.0, time, 0.0)); 
         
                double serviceTime = exponential(1/TS_CPU);
            
                Ts_cpu += serviceTime; 
                
               // see which core to get to service the next death
            
                if(Server1 == IDLE && Server2 == IDLE){
                    
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
                }
           
                else if(Server1 == BUSY && Server2 == IDLE){
                
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",2, time + serviceTime));
                    Server2 = BUSY;
                
                }
                else if(Server2 == BUSY && Server1 == IDLE){
                 
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
                }
  
            }
        
            else // or just add it to the disk queue
            {
//                
                 cpu_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, 0.0, time, 0.0)); 
            }
        }
        
        else // otherise make a network birth
            network_birth(time, OLD_BIRTH, arrival_time, left_queue);
       
        // schedule the next_disk death if there are more disk requests in the disk queue
        if(disk_buffer.size() != 0){
                 
                double serviceTime2 = exponential(1/TS_DISK);//exponential(1/TS_DISK);
                    
                Ts_disk += serviceTime2;
            
                schedule.insert(new CalenderEvent2("disk_death",0,time + serviceTime2));
                
                disk_server = BUSY;
            }
            
    }
      
    public void network_death(double time) {
        
        num_network_deaths++;
        
        Request2 remove = network_buffer.removeFirst();
        
        double arrival_time = remove.arrivalTime;
        double left_queue = remove.left_queue;
        
        
        Tw_network += time - left_queue;
      
        // if the cpu queue is empty you can predict immediately what will happen by calling the death event
       if (cpu_buffer.size() == 0 && Server2 == IDLE && Server1 == IDLE) {
          
           //you know that all networks go to the cpu, so add this request to the cpu
            cpu_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, 0.0, time, 0.0)); 
         
            double serviceTime = exponential(1/TS_CPU); 
                  
           // see which core to get to service the next death
                 
                if(Server1 == IDLE && Server2 == IDLE){
                    
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
   
                }
                else if(Server1 == BUSY && Server2 == IDLE){
                
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",2, time + serviceTime));
                    Server2 = BUSY;
                
                }
                else if(Server2 == BUSY && Server1 == IDLE){
                 
                    Ts_cpu += serviceTime;
                    schedule.insert(new CalenderEvent2("Death",1, time + serviceTime));
                    Server1 = BUSY;
      
                }
               
           
       }
       
       else if(cpu_buffer.size() != 0){ // or just add it to the cpu queue
          
           
           double serviceTime = exponential(1/TS_CPU);  
           cpu_buffer.add(new Request2(OLD_BIRTH, arrival_time, left_queue, 0.0, time, 0.0)); 
           
          
       }
       if(network_buffer.size() != 0){ // if the network buffer isnt empty, schedule the next net death
             
            //System.err.println("network_buffer.size() = " + network_buffer.size() );
            double serviceTime = exponential(1/TS_NETWORK);
            Ts_network += serviceTime;
          
            schedule.insert(new CalenderEvent2("network_death",0, time + serviceTime));
            
        }
    }
    
    // monitoring event!!
    
     public void monitorEvent(double time, double lambda){
 
      int size_of_cpubuf = cpu_buffer.size();
      int size_of_diskbuf = disk_buffer.size();
      int size_of_netbuf = network_buffer.size();
     
      if (Server1 == BUSY && Server2 == BUSY)                 // one server is busy so decrement 1 from the cpu
          
          size_of_cpubuf = (size_of_cpubuf+2);
       
      else if(Server1 == BUSY && Server2 == IDLE)            // both servers are busy so decrement 2 from cpu_buff count
          
           size_of_cpubuf = (size_of_cpubuf+1);
       
      else if(Server2 == IDLE && Server2 == BUSY)            // both servers are busy so decrement 2 from cpu_buff count
          
           size_of_cpubuf = (size_of_cpubuf+1);
      else
          size_of_cpubuf = size_of_cpubuf;  //both servers are idle
        
       // we want to schedule the next arrival time based on an exp distribution 
       
       
       q_cpu += size_of_cpubuf;
       
       q_disk += size_of_diskbuf;
       
       q_net += size_of_netbuf;
      
       q += (size_of_cpubuf + size_of_diskbuf + size_of_netbuf);
       
       double arrival_time = exponential(1);
        
       schedule.insert(new CalenderEvent2("Monitor",0, time + arrival_time));
      
    }
      
    public void printResults() {
        
        System.out.println("\nResults of this MM1 Simulator:");
        
        System.out.println("\nTq "+ Tq/serviced);
        
        System.out.println("\nq ="+ q/num_of_monitors);
        System.out.println("q_network ="+ q_net/num_of_monitors);
        System.out.println("q_disk = "+ q_disk/num_of_monitors);
        
        System.out.println("w_cpu ="+ w_cpu_average/num_death_resquests);
        
        
        System.out.println("\nTs_disk "+ Ts_disk/num_disk_requests);
        System.out.println("Ts_network "+ Ts_network/num_network_requests);
        System.out.println("Ts_cpu "+ Ts_cpu/num_death_resquests);
        
        //System.err.println("finding roe of net == "+ Ts_network / LAMBDA);
        
        System.out.println("\nTw_disk "+ Tw_disk/num_disk_deaths);
        System.out.println("Tw_net"+ Tw_network/num_disk_deaths);
        
     
        System.out.println("\nnum_of_monitors = " + num_of_monitors);
        System.out.println("num_disk_requests = " + num_disk_requests);
        
        System.out.println("num_network_requests =" +num_network_requests);
        System.out.println("Actual deaths =" + serviced);
        System.out.println("Births =" + requests);
        
    }
    
    public void runMM1() { 
       
       while(time < (SIMULATION_TIME)){
            
            CalenderEvent2 newEvent = (schedule.getMin());
             
            time = newEvent.time;
            
            if(newEvent.id == "Birth"){
                
                //System.out.println("Birth " + time);
                birthEvent(time);
                requests++;
    
            }
             else if(newEvent.id == "Death"){
                 
                 //System.out.println("Death " + time);
                
                 int core_cpu = newEvent.core_cpu;

                 // System.err.println("Death at core " + core_cpu +" at "+ time +"with " + cpu_buffer.size() +" items");
                 deathEvent(time, core_cpu);
                 num_death_resquests++;
                 
            }
             else if(newEvent.id == "network_death"){
                 
                 //System.out.println("net_death " + time);
                 network_death(time);
                 
            }
             
            else if(newEvent.id == "disk_death"){
                
                //System.out.println("disk_death " + time);
                disk_death(time);
                num_disk_requests++;
               
            }
            else if(newEvent.id == "Monitor"){
                
                //System.out.println("monitor " + time);
                monitorEvent(time, LAMBDA);
                num_of_monitors++;
               
            }

       }
     
    }
    
    public static void main(String[] args) { 
       
        double lambda = 40.0;
        double ts_cpu = 0.01;
        double ts_disk = 0.1;
        double ts_network = 0.025;
        double length = 100; 
      
        NetworkofQueues c1 = new NetworkofQueues(lambda,ts_cpu, ts_disk, ts_network, length);
        
        c1.runMM1();
        
        c1.printResults();
        
    }
    
}


 

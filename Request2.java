
// this is a class to create the event class for my eventual calender linked list
// megan horan

public class Request2{
    
    public int id;
   
    public double arrivalTime;
    
    public double left_queue;
    public double left_disk;
    public double left_network;
    public double left_universe;
    
    public Request2(int identity, double z, double s, double x, double a, double w) {
        
        this.id = identity;
        this.arrivalTime = z;
        this.left_queue = s;
        this.left_disk = x;
        this.left_network = a;
        this.left_universe = w;
        
    }
   
}
    

// this is a class to create the event class for my eventual calender linked list
// megan horan

public class Request{
    
    public int id;
    
    public double arrivalTime;
    public double leftQueue;
    public double endedService;
    
    public Request(int identity, double a, double s, double e) {
        
        this.id = identity;
        this.arrivalTime = a;
        this.leftQueue = s;
        this.endedService = e;
    }
    
}
    
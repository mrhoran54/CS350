// authoer: megan horan (mrhoran@bu.edu)

class MinHeap2 {
   
   private final int SIZE = 100;       // initial length of array
   private int next = 0;              // limit of elements in array
   private CalenderEvent2[] A = new CalenderEvent2[SIZE];   // implements tree by storing elements in level order
   
   // standard resize to avoid overflow
   
   private void resize() {
       
      CalenderEvent2 [] B = new CalenderEvent2[A.length*2];
      for(int i = 0; i < A.length; ++i)
         B[i] = A[i];
      A = B; 
   }
   
   // methods to move up and down tree as array
   
   private int parent(int i) { return (i-1) / 2; }
   private int lchild(int i) { return 2 * i + 1; }
   private int rchild(int i) { return 2 * i + 2; }
   
   private boolean isLeaf(int i) { return (lchild(i) >= next); }
   private boolean isRoot(int i) { return i == 0; }
   
   // standard swap, using indices in array
   private void swap(int i, int j) {
      CalenderEvent2 temp = A[i];
      A[i] = A[j];
      A[j] = temp;
   }
   
   // basic data structure methods
   
   public boolean isEmpty() {
      return (next == 0);
   }
   
   public int size() {
      return (next);
   }
   
   // insert an Article into array at next available location
   //    and fix any violations of heap property on path up to root
   
   public void insert(CalenderEvent2 k) {
      
      if(size() == A.length) resize(); 
      
      A[next] = k; 
      
      int i = next;
      int p = parent(i);
      
      while(!isRoot(i) && A[i].time < A[p].time) { //.time
         
         swap(i,p);
         i = p;
         p = parent(i);
         
      }
      
      ++next;
   }
   
   
   // Remove top (maximum) element, and replace with last element in level
   //    order; fix any violations of heap property on a path downwards
   
   public CalenderEvent2 getMin() { //Calender event
      
       --next;
      swap(0,next);                // swap root with last element
      int i = 0;                   // i is location of new key as it moves down tree
 
      // while there is a maximum child and element out of order, swap with max child
      int mc = minChild(i); 
      
      // // instead of comparing integers , you are comparing the cs of the articles in the certain index
      while(!isLeaf(i) && A[i].time > A[mc].time) {  //.time
          
         swap(i,mc);
         i = mc; 
         mc = minChild(i);
         

      }       
        return A[next];
   }
   

    // return index of maximum child of i or -1 if i is a leaf node (no children)
   
   int minChild(int i) {
      
      
      if(lchild(i) >= next)
         return -1;
      if(rchild(i) >= next)
         return lchild(i);
      //instead of comparing ints,
      
      else if(A[lchild(i)].time < A[rchild(i)].time)  //.time
         return lchild(i);
      else
         return rchild(i); 
   }
     
   // Apply heapsort to the array A. To use, fill A with keys and then call heapsort
   
   public  void heapSort() {
      next = 0;
      for(int i = 0; i < A.length; ++i)      // turn A into a heap
         insert(A[i]);
      for(int i = 0; i < A.length; ++i)      // delete root A.length times, which swaps max into
         getMin();                           //  right side of the array
   }

   // debug method
   
   private void printHeap() {
      for(int i = 0; i < A.length; ++i)
         System.out.print(A[i] + " ");
      System.out.println("\t next = " + next);
   }
   
   private void printHeapAsTree() {
      
      printHeapTreeHelper(0, ""); 
      System.out.println(); 
   }
   
   private void printHeapTreeHelper(int i, String indent) {
      if(i < next) {
         
         System.out.println("\nTimeof next event " + A[i].time);  //.time
         printHeapTreeHelper(rchild(i), indent + "   "); 
         System.out.println(indent + A[i]);
         printHeapTreeHelper(lchild(i), indent + "   "); 
      }
   }
   
  
}
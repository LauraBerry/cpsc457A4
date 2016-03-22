package cpsc457;

import cpsc457.doNOTmodify.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.*;

public class LinkedList<T> implements Iterable<T> {
 
    //####################
    //# Static Functions #
    //####################
    
    //We do not want the testers to have the freedom of specifying the comparison function
    //Thus, we will create wrappers for them that they can use and inside these wrappers
    //we will have the comparison function predefined
        //These two static wrappers, will simply call the sort method in the list passed as parameter,
        //and they pass the comparison function as well
    
    public static <T extends Comparable<T>> void par_sort(LinkedList<T> list) {
        list.par_sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public static <T extends Comparable<T>> void sort(LinkedList<T> list){
        list.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }
 
    //############
    //# LinkList #
    //############
    
    //Variables (attributes)
        //Head
        Node<T> head;
        
        //Tail
        Node<T> tail;
        
        //Size (not required)
        int size;
        
        //Critical Section
        Lock criticalSection;
        
    //Constructor
    public LinkedList() 
    {
        //Set head and tail to null
        head = null;
        tail = null;

        //Set size to zero
        size = 0;

        //Create new instance for the critical section
        criticalSection = new ReentrantLock();
    }

    //Returns the size of the list
    //Laura:either use meta data or a for loop 
    public int size() {
        criticalSection.lock();
        try
        {
           int temp=size;
            return temp;
        }
        finally
        {
            criticalSection.unlock();
        }

    }
    
    //Checks if the list is empty
    //keep in mind this has to be dynamic
    public boolean isEmpty() {
        criticalSection.lock();
        try
        {
            return(size == 0 || head == null);
        }
        finally
        {
            criticalSection.unlock();
        }
    }
    
    //Deletes all the nodes in the list
    public void clear() {
        //just set the head and tail to null (the garbage collector takes care of the rest)
            //cpp developers: be careful, you have to destroy them first
        criticalSection.lock();
        try
        {
            head = null;
            tail = null;
            size = 0;
        }
        finally
        {
            criticalSection.unlock();
        }
        
        //What if the merge sort is running now in a thread
            //I should not be able to delete the nodes (and vice versa)
            //Thus run this and everything else in a critical section
    }
    
    //Adds a new node to the list at the end (tail)
    public LinkedList<T> append(T t) {
        criticalSection.lock();
        try
        {
            Node<T> nodeT = new Node(t);
            
            //Check if it is empty 
            if(this.isEmpty())
            {
                //head = tail = t
                head = nodeT;
                tail = nodeT;
                
                head.next = tail;
                head.prev = null;
                
                tail.next = null;
                tail.prev = head;
                
            }
            //Else add to the tail and move the tail to the end
            else
            {                
                //tail.next = t    then        tail = t
                nodeT.prev = tail;
                tail.next = nodeT;
                tail = nodeT;
                tail.next = null;
            }

            //Do not forget to increment the size by 1 (if you have it as an attribute)
            size++;
            return this;
        }
        finally
        {
            criticalSection.unlock();
        }
    }

    //Gets a node's value at a specific index
    public T get(int index) {
        //Laura:we are assuming this linked list is zero indexed.

        T value = null;
        criticalSection.lock();
        try
        {
            //Create a new pointer that starts at the head
            Node<T> curr = head;

            //Make sure not to exceed the size of the list (else return null)
            if(index < size)
            {
                //Iterate through the list
                for(int i = 0; i<size; i++)
                {
                    //Keeps moving forward (pt = pt.next) for index times
                    curr = curr.next;
                }
                
                
                value = curr.value;
            }
            
            //then return that object
            return value;
        }
        finally
        {
            criticalSection.unlock();
        }
    }
    //Laura: remeber it wants the data in the node not the node itself.
    
    @Override
    public Iterator<T> iterator() {
        criticalSection.lock();
         try
         {
            Iterator<T> iterate = new Iterator<T>()
            {
                //iterator offset
                Node<T> offset = head;
                
                @Override
                public boolean hasNext()
                {
                    return(offset != null);
                }
                
                @Override
                public T next()
                {
                    T value = offset.value;
                    offset = offset.next;
                    return(value);
                }
            };
            return iterate;
        }
        finally
        {
            criticalSection.unlock();
        }
    }
    //Laura: be careful, iterator can end up being super slow, do not want this.
    //don't use get() in the iterator
    //use a for loop and .next.
    
    //The next two functions, are being called by the static functions at the top of this page
    //These functions are just wrappers to prevent the static function from deciding which
    //sorting algorithm should it use.
    //This function will decide which sorting algorithm it should use 
    //(we only have merge sort in this assignment)
    
    //Sorts the link list in serial
    private void sort(Comparator<T> comp) {
        criticalSection.lock();
        try
        {
            new MergeSort<T>(comp).sort(this); //Run this within the critical section (as discussed before)
        }
        finally
        {
            criticalSection.unlock();
        }
        
        //It might not allow you to use this inside critical
            //Create a final pointer = this then use that pointer
    }

    //Sorts the link list in parallel (using multiple threads)
    private void par_sort(Comparator<T> comp) {
        criticalSection.lock();
         try
         {
            new MergeSort<T>(comp).parallel_sort(this); //Run this within the critical section (as discussed before)
        }
        finally
        {
            criticalSection.unlock();
        }
    }

    //Merge sort
    static class MergeSort<T> {
    
        //Variables (attributes)
            //ExecutorService
            ExecutorService allThreads;
            
            //Depth limit
            int depthLimit;
            
            int poolSize = 1024;
            
            Lock criticalSection;
    
        //Comparison function
        final Comparator<T> comp;

        //Constructor
        public MergeSort(Comparator<T> comp) {
            // create a new pool of size poolSize 
            allThreads = Executors.newFixedThreadPool(poolSize);
            
            // depth limit calculation
            depthLimit = ((int) (Math.log(poolSize)/Math.log(2)));

            // initialize critical section
            criticalSection = new ReentrantLock();
            
            this.comp = comp;
        }

        //#####################
        //# Sorting functions #
        //#####################
        //The next two functions will simply call the correct function 
        //to merge sort the link list and then they will fix its 
        //attributes (head and tail pointers)
        
        public void sort(LinkedList<T> list)
        {
            // call correct function
            Node<T> head = msort(list.head);

            list.head = head;
            // fix list attributes (head and tail pointers)
            
        }

        public void parallel_sort(LinkedList<T> list)
        {
            Node<T> head = parallel_msort(list.head,getDepth());

            list.head = head;
            /*DEBUG
            System.out.println("poolsize: " + getPool());*/
            // fix list attributes (head and tail pointers)            
        }
        
        public void printer(Node<T> a)
        {
            while(a!=null)
            {
                System.out.print(a.value);
                System.out.print(", ");
                a=a.next;
            }
            System.out.println();
        }
        
        //#########
        //# Steps #
        //#########
        
        //The main merge sort function (parrallel_msort and msort)
            //Split the list to two parts
            //Merge sort each part
            //Merge the two sorted parts together

        // sequential merge sort
        public Node<T> msort(Node<T> list)
        {
            // if the list has only one element or no elements
            // then it is already sorted
            if(list==null || list.next==null)
            {
                return list;
            }
            
            //Split the list to two parts
            Pair<Node<T>,Node<T>> pair = split(list);
            Node<T> head1 = pair.fst();
            Node<T> head2 = pair.snd();
            
            /* DEBUG */
            //System.out.println("pair: " + head1.value + " " + head2.value);
            
            //Merge sort each part
            Node<T> list1 = msort(head1);
            Node<T> list2 = msort(head2);
            
            //Merge the two sorted parts together
            Node<T> merged = merge(list1,list2);
            
            return merged;
        }
        
        // parallel merge sort
        public Node<T> parallel_msort(Node<T> list,int depth)
        //public LinkedList<T> parallel_msort(LinkedList<T> list)
        {
            
            if(list==null||list.next==null)
            {
                return list;
            }
            
            //Split the list to two parts
            Pair<Node<T>,Node<T>> pair = split(list);
            Node<T> head1 = pair.fst();
            Node<T> head2 = pair.snd();
                        
            Node<T> list1=null;
            Node<T> list2=null;
            
            Future<Node<T>> future1 = null;
            Future<Node<T>> future2 = null;
            
            // if we can still use threads
            if(depth >= 1)
            {
                //System.out.println("head1\t" + head1.value +"\thead2\t" + head2.value + "\tdepth\t" + getDepth() + " " + getPool());

                //Merge sort each part
                future1 = allThreads.submit(new Callable()
                {
                   public Node<T> call() throws Exception
                   {
                       return parallel_msort(head1,depth-1);
                   }
                });
                
                //Merge sort each part
                future2 = allThreads.submit(new Callable()
                {
                    public Node<T> call() throws Exception
                    {
                        return parallel_msort(head2,depth-1);
                    }
                });
                
                // get return values from future
                try
                { 
                    list1 = future1.get();
                    list2 = future2.get();
                    
                }
                catch(Exception e)
                {
                    System.out.println("Exception: " + e);
                }
            }
            else
            {
                list1 = parallel_msort(head1,0);
                list2 = parallel_msort(head2,0);
            }
            
            //Merge the two sorted parts together
            Node<T> merged = merge(list1,list2);
            
            return merged;
        }

        //Splitting function
            //Run two pointers and find the middle of the a specific list
            //Create two new lists (and break the link between them)
            //It should return pair (the two new lists)
        public Pair<Node<T>,Node<T>> split(Node<T> head)
        {
            Node<T> list1 = head;
            Node<T> list2 = head;
            
            while(list1!=null && list1.next!=null)
            {
                list1=list1.next.next;
                list2=list2.next;
            }
            
            list2.prev.next=null;
            list2.prev=null;
            
            Pair<Node<T>,Node<T>> pair = new Pair(head,list2);
            
            return pair;
        }
        
        //Merging function
            //1- Keep comparing the head of the two link lists
            //2- Move the smallest node to the new merged link list
            //3- Move the head on the list that lost this node
            
            //4- Once one of the two lists is done, append the rest of the 
            //     second list to the tail of the new merged link list
            
        public Node<T> merge(Node<T> head1, Node<T> head2)
        {      
            Node<T> temp = new Node(999999);
            Node<T> curr = temp;
            Node<T> sorted = curr;
            //1- Keep comparing the head of the two link lists
            while(head1!=null && head2!=null)
            {
                // if head1<=head2
                if(comp.compare(head1.value, head2.value)<0)
                {
                    /* DEBUG 
                    System.out.println(head1.value + "<=" + head2.value);*/
                    
                    sorted.next = head1;
                    head1 = head1.next;
                }
                // if head1>head2
                else
                {
                    /* DEBUG 
                    System.out.print(head1.value + ">" + head2.value);*/
                    
                    sorted.next = head2;
                    
                    /* DEBUG 
                    System.out.println("\t\ttemp.next\t" + sorted.next.value);*/
                    
                    head2 = head2.next;
                }
               
                /* DEBUG 
                System.out.println("temp before\t" + sorted.value);*/
                
                sorted = sorted.next;
                
                /* DEBUG 
                System.out.println("temp after\t" + sorted.value);*/
            }
            
            if(head1==null)
            {
                sorted.next = head2;
            }
            else
            {
                sorted.next = head1;
            }
            
            return curr.next;
        }
    
        public int getDepth()
        {
            criticalSection.lock();
            try
            {
                return depthLimit;
            }
            finally
            {
                criticalSection.unlock();
            }
        }
    }

    public static class Node<T>
    {
        T value;
        Node<T> next;
        Node<T> prev;
        
        public Node()
        {
            value = null;
            next = null;
            prev = null;
        }
        
        public Node(T v)
        {
            value = v;
        }
    }
 
}

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
        return  size;
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
                    
                    System.out.println(curr.value);
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
	
		new MergeSort<T>(comp).sort(this); //Run this within the critical section (as discussed before)
		
		//It might not allow you to use this inside critical
			//Create a final pointer = this then use that pointer
    }

	//Sorts the link list in parallel (using multiple threads)
    private void par_sort(Comparator<T> comp) {
		new MergeSort<T>(comp).parallel_sort(this); //Run this within the critical section (as discussed before)
    }

	//Merge sort
    static class MergeSort<T> {
    
        //Variables (attributes)
            //ExecutorService
            ExecutorService allThreads;
            
            //Depth limit
            int depthLimit;
            
            int poolSize = 16;
    
        //Comparison function
        final Comparator<T> comp;

        //Constructor
        public MergeSort(Comparator<T> comp) {
            allThreads = Executors.newFixedThreadPool(poolSize);
            depthLimit = poolSize/2; // arbitrary calculation for now
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
			//Laura: do this one first
			//Laura: do NOT create a new linked list here!!
			// call correct function
			Node<T> head = msort(list.head);

            list.head = head;
			// fix list attributes (head and tail pointers)
			
		}

		public LinkedList<T> parallel_sort(LinkedList<T> list)
		{
			// call correct function
			//LinkedList<T> sortedList = parallel_msort(list);

			// fix list attributes (head and tail pointers)
			return list;
			
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
		public Node<T> parallel_msort(Node<T> list)
		//public LinkedList<T> parallel_msort(LinkedList<T> list)
		{
			if(list==null || list.next==null)
			{
				return list;
			}
			LinkedList<T> currentList= new LinkedList<T>();
			int listSize= currentList.size();
	
			//break list into 16 parts
			Pair<Node<T>,Node<T>> pair = split(list);
		    Node<T> head1 = pair.fst();
		    Node<T> head2 = pair.snd();
			pair = split(head1);
		    Node<T> head3 = pair.fst();
		    Node<T> head4 = pair.snd();
			pair = split(head2);
		    Node<T> head5 = pair.fst();
		    Node<T> head6 = pair.snd();
			pair = split(head3);
		    Node<T> head7 = pair.fst();
		    Node<T> head8 = pair.snd();
			pair = split(head5);
		    Node<T> head9 = pair.fst();
		    Node<T> head10 = pair.snd();
			pair = split(head4);
		    Node<T> head11 = pair.fst();
		    Node<T> head12 = pair.snd();
			pair = split(head6);
		    Node<T> head13 = pair.fst();
		    Node<T> head14 = pair.snd();
			pair = split(head14);
		    Node<T> head15 = pair.fst();
		    Node<T> head16 = pair.snd();

			//code the threads should complete starts
 			 	/*
			for (all threads in allThreads)
			{
					try
					{
						List<Future<T>> mergers = allThreads.invokeAll(parallel_msort(threads));
					}
					catch (Exception e)
					{
						System.out.println("exception thrown");
					}
			}*/
			Node<T> list1 = parallel_msort(head1);
			Node<T> list2 = parallel_msort(head2);
			Node<T> list3 = parallel_msort(head3);
			Node<T> list4 = parallel_msort(head4);
			Node<T> list5 = parallel_msort(head5);
			Node<T> list6 = parallel_msort(head6);
			Node<T> list7 = parallel_msort(head7);
			Node<T> list8 = parallel_msort(head8);
			Node<T> list9 = parallel_msort(head9);
			Node<T> list10 = parallel_msort(head10);
			Node<T> list11 = parallel_msort(head11);
			Node<T> list12 = parallel_msort(head12);
			Node<T> list13 = parallel_msort(head13);
			Node<T> list14 = parallel_msort(head14);
			Node<T> list15 = parallel_msort(head15);
			Node<T> list16 = parallel_msort(head16);
/*please note the threads should do this but for now i have it here to help the rest of the code work until we can fix the threads to work*/


			/*
			int j=0;
			List <Node<T>> merge1;
			List <Node<T>> merge2;
			List <Node<T>> merge3;
			for (int i=0 ; i<8; i++)
			{
				merge1[i]=merge(mergers[j], mergers[j+1])
				j=j+2;
			}
			j=0;
			for (int k=0; k<4; k+2)
			{
				merge2 [i]= merge(merge1[j], merge1[j+1]);
				j++;
			}
			j=0;
			for (int m=0; m<2; m++)
			{
				merge3[i]=merge(merge1[j], merge1[j+1]);
			}
			merged = merge(merged3[0], merged3[1]);
			return merged
			
			//something like this?
			*/
            //Merge the 16 sorted parts together
            Node<T> merged1 = merge(list1,list2);
			Node<T> merged2 = merge(list3,list4);
			Node<T> merged3 = merge(list5,list6);
			Node<T> merged4 = merge(list7,list8);
			Node<T> merged5 = merge(list9,list10);
			Node<T> merged6 = merge(list11,list12);
			Node<T> merged7 = merge(list13,list14);
			Node<T> merged8 = merge(list15,list16);

			// merged the 8 pre-merged parts together
			Node<T> mergedMerged1 = merge(merged1, merged2);
			Node<T> mergedMerged2 = merge(merged3, merged4);
			Node<T> mergedMerged3 = merge(merged5, merged6);
			Node<T> mergedMerged4 = merge(merged7, merged8);

			//merge the 4 pre-pre-merged parts together
			Node<T> mergedMergedMerged1= merge(mergedMerged1, mergedMerged2);
			Node<T> mergedMergedMerged2 = merge(mergedMerged3, mergedMerged4);

			//finally merge the last 2 parts together.
			Node<T> merged= merge(mergedMergedMerged1, mergedMergedMerged2);
			/* plase note: this will probably be doable in a loop once the threads are working but for now this is how i had to do it.*/
            
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
            
            int size = 0;
            
            while(list2!=null)
            {
                list2 = list2.next;
                size++;
            }
            
            int half = size/2;
            
            list2 = head;
            
            for(int i=0;i<half;i++)
            {
                list2 = list2.next;
            }
            
            list2.prev.next = null;
            list2.prev = null;
            
            Pair<Node<T>,Node<T>> pair = new Pair(list1,list2);
            
            return pair;
        }
		
		//Merging function
			//1- Keep comparing the head of the two link lists
			//2- Move the smallest node to the new merged link list
			//3- Move the head on the list that lost this node
			
			//4- Once one of the two lists is done, append the rest of the 
			//	 second list to the tail of the new merged link list
            
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
                System.out.println("temp before\t" + sorted.value); */
                
                sorted = sorted.next;
                
                /* DEBUG
                System.out.println("temp after\t" + sorted.value); */
            }
            
            if(head1==null)
            {
                sorted.next = head2;
            }
            else
            {
                sorted.next = head1;
            }
            
            /* DEBUG
            System.out.println("final curr\t" + curr.next.value);
            Node<T> current = curr.next;
            System.out.print("current\t\t");
            while(current!=null)
            {
                System.out.print(current.value);
                current=current.next;
            }
            System.out.println("\n----------------------------------");
            */
            return curr.next;
        }
            //1- Keep comparing the head of the two link lists
            //2- Move the smallest node to the new merged link list
            //3- Move the head on the list that lost this node
            
            //4- Once one of the two lists is done, append the rest of the 
            //     second list to the tail of the new merged link list
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

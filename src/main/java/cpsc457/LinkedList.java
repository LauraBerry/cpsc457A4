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
		//Tail
		//Size (not required)
		//Critical Section
		//need to create a locker, so i can say isLocked or isn't Locked.
	Node<T> head, tail;
	int size;
 
	//Constructor
    public LinkedList() 
	{
		//Set head and tail to null
		head=null;
		tail= null;

		//Set size to zero
		size=0;

		//Create new instance for the critical section
    }

	//Returns the size of the list
	
    public int size() {
        return  size;
    }
	//either use meta data or a for loop 
	
    //Returns head
	public Node<T> getHead()
	{
		return head;
	}
	
    // Returns tail
	public Node<T> getTail()
	{
		return tail;
	}
    
	//Checks if the list is empty
	//keep in mind this has to be dynamic
	public boolean isEmpty() {
        if(size==0|| this.head==null)
		{
			return true;		
		}
		else
		{
			return false;
		}
    }
	
	//Deletes all the nodes in the list
	public void clear() {
		//just set the head and tail to null (the garbage collector takes care of the rest)
			//cpp developers: be careful, you have to destroy them first
			head = null;
			tail = null;
			size=0;
		
		//What if the merge sort is running now in a thread
			//I should not be able to delete the nodes (and vice versa)
			//Thus run this and everything else in a critical section
    }
	
	//Adds a new node to the list at the end (tail)
    public LinkedList<T> append(T t) {
        Node<T> nodeT = new Node(t);
		//Check if it is empty 
			//head = tail = t
        
		if(this.isEmpty())//isEmpty())
		{
			head = nodeT;
			tail = nodeT;
		}
		else
		{
			tail.next= nodeT;
			nodeT.prev=tail;
			tail=nodeT;
		}
		size++;
        return this;
		//Else add to the tail and move the tail to the end
			//tail.next = t    then		tail = t
		
		//Do not forget to increment the size by 1 (if you have it as an attribute)
    }

	//Gets a node's value at a specific index
    public T get(int index) {
		//Laura:we are assuming this linked list is zero indexed.

		//Iterate through the list
			//Create a new pointer that starts at the head
			//Keeps moving forward (pt = pt.next) for index times
			//then return that object
		if(index< size)
		{
			Node<T> result=head;		
			for (int i=0; i<index; i++)
			{
                result=result.next;
			}
			return result.value;
		}
		//Make sure not to exceed the size of the list (else return null)
		else
		{
			return null;
		}
    }
	//Laura: remeber it wants the data in the node not the node itself.
	
	@Override
    public Iterator<T> iterator() {
		Node curr= this.head;
		while (curr.next!= null)
		{
			curr=curr.next;
		}

		return null;
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
			Collection mintCondition;


			//Depth limit
				// can have a variable inialized in the sort function chosen.
			int depthLimit; //where do we decide this?
		//Comparison function
		final Comparator<T> comp;

		//Constructor
		public MergeSort(Comparator<T> comp) {
			allThreads=Executors.newFixedThreadPool(12);
			this.comp = comp;
		//passes the comparison then say either sort or par_sort the list.
		//both are currently using the main thread.
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
			LinkedList<T> sortedList = msort(list);

			// fix list attributes (head and tail pointers)
			
		}

		public void parallel_sort(LinkedList<T> list)
		{
			// call correct function
			LinkedList<T> sortedList = parallel_msort(list);

			// fix list attributes (head and tail pointers)
			
		}
		
		//#########
		//# Steps #
		//#########
		
		//The main merge sort function (parrallel_msort and msort)
			//Split the list to two parts
			//Merge sort each part
			//Merge the two sorted parts together

		// sequential merge sort
		public LinkedList<T> msort(LinkedList<T> list)
		{
            // if the list has only one element or no elements
            // then it is already sorted
            if(list.size() < 1)
            {
                System.out.println("list too small");

                return list;
            }
            
			LinkedList<T> sortedList;

			// split list
            Pair<LinkedList<T>,LinkedList<T>> split = split(list);
            LinkedList<T> list1 = split.fst();
            LinkedList<T> list2 = split.snd();
            
			//recurse
			if (list1.size!=1)
			{
				list1=msort(list1);
			}
			if (list2.size !=1)
			{
				list2=msort(list2);
			}
			           
			// merge	
            sortedList = merge(list1, list2);
            
            return sortedList;
		}
		
		// parallel merge sort
		public LinkedList<T> parallel_msort(LinkedList<T> list)
		{
			// if the list has only one element or no elements
            // then it is already sorted
            if(list.size() < 1)
            {
                System.out.println("list too small");

                return list;
            }
            
			LinkedList<T> sortedList;
			LinkedList<T> currentList = new LinkedList();

			if(list.size()<12)
			{
				T currNodeValue=list.head.value;
				Node<T> currNode=list.head;
				for (int i=0; i<list.size; i++)
				{
					currentList.append(currNodeValue);
					currNode=currNode.next;
					currNodeValue=currNode.value;			
					//Thread.submit(currentList);
				}
			}
			else if (list.size()>12)
			{
				if(list.size()%12==0)
				{
					int sectionSize=list.size()/12;
					T currNodeValue=list.head.value;
					Node<T> currNode=list.head;
					for (int i=0; i<12; i++)
					{
						for(int j=0; j<sectionSize; j++)
						{
							currentList.append(currNodeValue);
							currNode=currNode.next;
							currNodeValue=currNode.value;			
						}
						//Thread.submit(currentList);
					}
					try
					{
						List<Future<T>> mergers = allThreads.invokeAll(mintCondition);
					}
					catch (Exception e)
					{
						System.out.println("exception thrown");
					}
// each thread gets a section
				}
				else
				{
					int addOn=list.size()%12;
					int sectionSize=list.size()/12;
					int lastSectionSize=sectionSize+addOn;

					T currNodeValue=list.head.value;
					Node<T> currNode=list.head;

					for (int i=0; i<11; i++)
					{
						for(int j=0; j<sectionSize; j++)
						{
							currentList.append(currNodeValue);
							currNode=currNode.next;
							currNodeValue=currNode.value;			
						}
						//Thread.submit(currentList);
					}
					for(int j=0; j<lastSectionSize; j++)
						{
							currentList.append(currNodeValue);
							currNode=currNode.next;
							currNodeValue=currNode.value;			
						}
						//Thread.submit(currentList);
						//mintCondition.add(Thread);
					//the last thread gets the larger section.
					try
					{
						List<Future<T>> mergers = allThreads.invokeAll(mintCondition);
					}
					catch (Exception e)
					{
						System.out.println("exception thrown");
					}
				}
			}
			else
			{
				T currNodeValue=list.head.value;
				Node<T> currNode=list.head;
				for (int i=0; i<12; i++)
				{	
					currentList.append(currNodeValue);
					currNode=currNode.next;
					currNodeValue=currNode.value;			
					//Thread.submit(currentList);
				}
				try
				{
					List<Future<T>> mergers = allThreads.invokeAll(mintCondition);
				}
				catch (Exception e)
				{
					System.out.println("exception thrown");
				}
			}

			// split list
            Pair<LinkedList<T>,LinkedList<T>> split = split(list);
            LinkedList<T> list1 = split.fst();
            LinkedList<T> list2 = split.snd();
            
			//recurse
			if (list1.size!=1)
			{
				list1=msort(list1);
			}
			if (list2.size !=1)
			{
				list2=msort(list2);
			}
			           
			// merge	
            sortedList = merge(list1, list2);
            
            return sortedList;

		}

		//Splitting function
			//Run two pointers and find the middle of the a specific list
			//Create two new lists (and break the link between them)
			//It should return pair (the two new lists)
		
		public Pair<LinkedList<T>,LinkedList<T>> split(LinkedList<T> list)
		{
            // list1
			Node curr=list.getHead();
			int i=0;
			int j=0;
			while(curr!=list.getTail())
			{
				curr=curr.next;
				i++;
				j=j+2;
			}
			
            LinkedList<T> list1 = new LinkedList();
            list1.head=list.getHead();
            list1.tail=new Node(list.get(i));
            list1.getTail().next = null;
            
            // list2
            LinkedList<T> list2 = new LinkedList();
            list2.head=list1.tail.next;
            list2.tail=list.getTail();	
            list2.getHead().prev = null;
            
            // initialize pair containing head of two lists
            Pair<LinkedList<T>,LinkedList<T>> pair = new Pair(list1.head,list2.head);
            
            return pair;//pair <T><T> obj - function; obj.fst(); obj.scnd();
		}
		
		//this will return 2 heads inidcating the 2 halfs.
	
		
		//Merging function
			//1- Keep comparing the head of the two link lists
			//2- Move the smallest node to the new merged link list
			//3- Move the head on the list that lost this node
			
			//4- Once one of the two lists is done, append the rest of the 
			//	 second list to the tail of the new merged link list
            
        public LinkedList<T> merge(LinkedList<T> list1, LinkedList<T> list2)
        {
            LinkedList<T> sortedList = new LinkedList();
            
            while(!list1.isEmpty() || !list2.isEmpty())
            {
                // if list1<=list2
				 
                if((list1.getHead()).compareTo(list2.getHead()))
                {
                    //Move the smallest node to the new merged link list
                    sortedList.append(list1.getHead().value);
                    
                    //Move the head on the list that lost this node
                    list1.head = list1.getHead().next;
                    list1.getHead().prev = null;
                }
                else
                {
                    //Move the smallest node to the new merged link list
                    sortedList.append(list2.getHead().value);
                    
                    //Move the head on the list that lost this node
                    list2.head = list2.getHead().next;
                    list2.getHead().prev = null;
                }
            } // end of while loop
            
            //append the rest of the second list to the tail of the new merged link list
            if(list1.isEmpty())
            {
                sortedList.append(list2.getHead().value);
            }
            else
            {
                sortedList.append(list1.getHead().value);
            }
            
            return sortedList;
        }
	}

	public static class Node<T>
	{
		Node<T> prev;
		Node<T> next;
		T value;
		
		Node(T value)
		{
		    this.value = value;
		    prev = null;
		    next = null;
		}
		//node1.value comparison node2.value;
        
        public boolean compareTo(Node<T> node)
        {
            Integer node1 = (Integer) this.value;
            Integer node2 = (Integer) node.value;
            return(node1<=node2);
        }
	}

 
}

package cpsc457;

import cpsc457.doNOTmodify.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node<T>
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
}

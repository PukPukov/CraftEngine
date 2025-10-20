package ru.mrbedrockpy.craftengine.server.network;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.LinkedList;

public class ConcurrentQueue<T> {
    private final LinkedList<T> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    
    public void add(T item) {
        lock.lock();
        try {
            queue.addLast(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public T poll() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            return queue.removeFirst();
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
package com.baidu.idl.main.facesdk;

import java.util.LinkedList;

public class FaceQueue {
    public static FaceQueue getInstance() {
        return HolderClass.instance;
    }

    private static class HolderClass {
        private static final FaceQueue instance = new FaceQueue(1);
    }

    private int nThreads;
    private PoolWorker[] threads;
    private LinkedList queue = null;

    public FaceQueue(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedList();
        threads = new PoolWorker[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    r = (Runnable) queue.removeFirst();
                }
                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                } catch (RuntimeException e) {
                    // You might want to log something here
                    e.printStackTrace();
                }
            }
        }
    }
}
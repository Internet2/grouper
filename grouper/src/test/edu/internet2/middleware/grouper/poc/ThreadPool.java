package edu.internet2.middleware.grouper.poc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ThreadPool {

  /**
   * @author mchyzer
   *
   */
  public static class CallableWorkerThread implements
    Callable<Integer> {
    /**
     * 
     */
    private int workerNumber;
  
    /**
     * @param number
     */
    CallableWorkerThread(int number) {
        workerNumber = number;
    }
  
    public Integer call() {
        for (int i = 0; i <= 100; i += 20) {
            // Perform some work ...
            System.out.println("Worker number: " + workerNumber
                + ", percent complete: " + i + ", thread: " + Thread.currentThread().getName() );
            try {
                Thread.sleep((int)(Math.random() * 1000));
            } catch (InterruptedException e) {
            }
        }
        return(workerNumber);
    }
  }
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    
    args = new String[]{"100"};
    
    int numWorkers = Integer.parseInt(args[0]);

    ExecutorService tpes =
        Executors.newCachedThreadPool();

    CallableWorkerThread workers[] = 
        new CallableWorkerThread[numWorkers];
    Future futures[] = new Future[numWorkers];
    
    for (int i = 0; i < numWorkers; i++) {
        workers[i] = new CallableWorkerThread(i);
        Thread.sleep(50);
        futures[i]=tpes.submit(workers[i]);
    }
    for (int i = 0; i < numWorkers; i++) {
        try {
            System.out.println("Ending worker: " +
                futures[i].get());
        } catch (Exception e) {}
    }

  }

}

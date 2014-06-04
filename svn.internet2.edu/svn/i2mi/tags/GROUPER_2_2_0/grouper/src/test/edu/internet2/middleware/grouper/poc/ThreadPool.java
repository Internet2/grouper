/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
                + ", percent complete: " + i );
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
  public static void main(String[] args) {
    int numWorkers = Integer.parseInt(args[0]);

    ExecutorService tpes =
        Executors.newCachedThreadPool();
    CallableWorkerThread workers[] = 
        new CallableWorkerThread[numWorkers];
    Future futures[] = new Future[numWorkers];
    
    for (int i = 0; i < numWorkers; i++) {
        workers[i] = new CallableWorkerThread(i);
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

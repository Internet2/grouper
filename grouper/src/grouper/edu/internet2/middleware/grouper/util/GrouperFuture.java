/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;


/**
 * like a normal future but keeps a reference to the callable, 
 * and exceptions are wrapped in RuntimeException
 * @param <T> type of return
 */
public class GrouperFuture<T> implements Future<T> {

  /**
   * enclosed future
   */
  private Future<T> future;
  
  /**
   * callable for this future
   */
  private Callable callable;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperFuture.class);
  
  /**
   * @param theFuture 
   * @param theCallable 
   */
  public GrouperFuture(Future theFuture, Callable theCallable) {
    this.future = theFuture;
    this.callable = theCallable;
  }
  
  /**
   * @return the grouperCallable
   */
  public Callable getCallable() {
    return this.callable;
  }

  /**
   * if grouper callable, this is a convenience method for getting that type
   * @return the grouperCallable
   */
  public GrouperCallable getGrouperCallable() {
    if (!(this.callable instanceof GrouperCallable)) {
      throw new RuntimeException("Not GrouperCallable! " + (this.callable == null ? null : this.callable.getClass()));
    }
    return (GrouperCallable)this.callable;
  }

  /**
   * @see java.util.concurrent.Future#cancel(boolean)
   */
  public boolean cancel(boolean mayInterruptIfRunning) {
    return this.future.cancel(mayInterruptIfRunning);
  }

  /**
   * @see java.util.concurrent.Future#isCancelled()
   */
  public boolean isCancelled() {
    return this.future.isCancelled();
  }

  /**
   * @see java.util.concurrent.Future#isDone()
   */
  public boolean isDone() {
    return this.future.isDone();
  }

  /**
   * @see java.util.concurrent.Future#get()
   */
  public T get() {
    try {
      return this.future.get();
    } catch (Exception e) {
      GrouperCallable.throwRuntimeException(e);
    }
    throw new RuntimeException("shouldnt get here");
  }

  /**
   * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
   */
  public T get(long timeout, TimeUnit unit) {
    try {
      return this.future.get(timeout, unit);
    } catch (Exception e) {
      GrouperCallable.throwRuntimeException(e);
    }
    throw new RuntimeException("shouldnt get here");
  }

  /**
   * 
   * @param future
   * @param secondsToWait -1 to wait forever
   * @return true if done, false if not (can also call future.isDone(), and future.get())
   */
  public static boolean waitForJob(GrouperFuture<?> future, int secondsToWait) {
    
    long millisStart = System.currentTimeMillis();
    
    boolean waitForever = secondsToWait < 0;
    
    int seconds = 0;
    
    while(true) {
      long now = System.currentTimeMillis();
      if (!waitForever && ((now - millisStart)/1000 > secondsToWait)) {
        break;
      }
      
      long waitUntil = 30+millisStart + ((seconds+1)*1000);
      long sleepFor = Math.max(waitUntil-now, 30);
      GrouperUtil.sleep(sleepFor);
      if (future.isDone()) {
        future.get();
        return true;
      }
      seconds++;
    }
    return false;
  }
  
  /**
   * relies on the callable being a GrouperCallable.  make sure there arent more threads than the max.
   * pass in 0 to wait for all.
   * @param futures
   * @param threadPoolSize
   * @param callablesWithProblems pass in a list to capture which jobs had problems.  if null, then jsut throw
   * exceptions as they happen
   */
  public static void waitForJob(List<GrouperFuture> futures, int threadPoolSize, List<GrouperCallable> callablesWithProblems)  {
    OUTER: while (futures.size() > threadPoolSize) {
      
      int futureToRemove = -1;
      GrouperFuture grouperFuture = null;

      //0 means remove all, dont care which is first
      if (threadPoolSize == 0) {
        futureToRemove = 0;
        grouperFuture = futures.get(0);
      } else {
        //find one thats done
        for (int i=0;i<futures.size();i++) {
          grouperFuture = futures.get(i);
          if (grouperFuture.isDone()) {
            futureToRemove = i;
            break;
          }
        }
        
        //didnt find a job hat done?  wait a bit and try again
        if (futureToRemove == -1) {
          GrouperUtil.sleep(5);
          continue OUTER;
        }
      }
      
      try {
        //this will throw exception if necessary and wait if hadnt waited
        grouperFuture.get();
        
      } catch (RuntimeException e) {

        //decorate some info...
        GrouperCallable grouperCallable = grouperFuture.getGrouperCallable();
        GrouperUtil.injectInException(e, "Problem in job: " + grouperCallable.getLogLabel());

        if (callablesWithProblems != null) {
          LOG.warn("Non fatal problem with callable.  Will try again not in thread", e);
          callablesWithProblems.add(grouperFuture.getGrouperCallable());
        } else {
          //not capturing, just throw
          throw e;
        }

      }
      futures.remove(futureToRemove);
    }
  }


}

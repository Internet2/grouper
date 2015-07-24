/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * @param <T>
 */
public abstract class GrouperCallable<T> implements Callable<T> {

  /**
   * store the state of thread locals that need to propagate to threads
   */
  private GrouperThreadLocalState grouperThreadLocalState = new GrouperThreadLocalState();
  
  /**
   * describes the callable
   * @return the logLabel
   */
  public String getLogLabel() {
    return this.logLabel;
  }

  /**
   * note, call this in your grouper session, this runs in current thread
   * @param callablesWithProblems
   */
  public static void tryCallablesWithProblems(Collection<GrouperCallable> callablesWithProblems) {
    
    RuntimeException problem = null;

    int callablesWithProblemsCount = 0;
    
    for (GrouperCallable grouperCallable : GrouperUtil.nonNull(callablesWithProblems)) {
      
      try {
        grouperCallable.callLogic();
      } catch (RuntimeException re) {
        
        callablesWithProblemsCount++;
        
        if (problem == null) {
          problem = re;
        } else {
          LOG.error("Problem with callable: " + grouperCallable.getLogLabel());
        }
      }
      
    }
    
    if (problem != null) {
      GrouperUtil.injectInException(problem, callablesWithProblemsCount 
          + " callables out of problem count " + callablesWithProblems.size() + " had problems running outside of threads!");
      throw problem;
    }
  }
  
  /**
   * convert exception
   * @param throwable
   */
  public static void throwRuntimeException(Throwable throwable) {

    //this isnt good, exception
    if (throwable instanceof ExecutionException) {
      //unwrap it
      ExecutionException executionException = (ExecutionException)throwable;
      if (executionException.getCause() != null) {
        //the underlying exception is here... might be runtime
        throwable = executionException.getCause();
      }
    }
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException)throwable;
    }
    throw new RuntimeException(throwable);

  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperCallable.class);

  /**
   * grouper session subject or null if none.  keep subject and not session in case it gets stopped in another thread
   */
  private Subject grouperSessionSubject;
  
  /** loglabel */
  private String logLabel;

  /**
   * 
   */
  static int numberOfThreads = 0;
  
  /**
   * @see java.util.concurrent.Callable#call()
   */
  public final T call() throws Exception {
    
    long subStartNanos = -1;
    
    //store the old
    GrouperThreadLocalState oldGrouperThreadLocalState = new GrouperThreadLocalState();
    oldGrouperThreadLocalState.storeCurrentThreadLocals();
    
    try {
      if (LOG.isDebugEnabled()) {
        subStartNanos = System.nanoTime();
        synchronized (GrouperCallable.class) {
          numberOfThreads++;
        }
      }
      //propagate thread locals
      this.grouperThreadLocalState.assignCurrentThreadLocals();
      return this.callLogicWithSessionIfExists();
    } finally {
      if (LOG.isDebugEnabled()) {
        synchronized (GrouperCallable.class) {
          long nanos = System.nanoTime() - subStartNanos;
          long millis = nanos / 1000000;
          LOG.debug("Threads: " + numberOfThreads + ", " + this.logLabel + ", time in millis: " + millis);
          numberOfThreads--;
        }
      }
      //assign the old thread local state, shouldnt really matter since thread if going back in pool
      oldGrouperThreadLocalState.assignCurrentThreadLocals();
    }
    
  }

  /**
   * Computes a result
   *
   * @return computed result
   */
  public final T callLogicWithSessionIfExists() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);

    //if we already have the same session open, use it
    boolean sameSubjectInSession = grouperSession != null && SubjectHelper.eq(grouperSession.getSubject(), this.grouperSessionSubject);
    if (this.grouperSessionSubject != null && !sameSubjectInSession) {
      grouperSession = GrouperSession.start(this.grouperSessionSubject, false);
      return (T)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          return GrouperCallable.this.callLogic();
        }
      });
      
    }
    return this.callLogic();
  }

  /**
   * Computes a result
   *
   * @return computed result
   */
  public abstract T callLogic();
  
  /**
   * @param theGrouperSession
   * @param theLogLabel
   */
  public GrouperCallable(String theLogLabel, GrouperSession theGrouperSession) {
    this.logLabel = theLogLabel;
    this.grouperSessionSubject = theGrouperSession == null ? null : theGrouperSession.getSubject();
    //keep track of important thread locals
    this.grouperThreadLocalState.storeCurrentThreadLocals();
  }

  /**
   * construct with log label, use the static session if it exists
   * @param theLogLabel
   */
  public GrouperCallable(String theLogLabel) {
    this.logLabel = theLogLabel;
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    this.grouperSessionSubject = grouperSession == null ? null : grouperSession.getSubject();
    //keep track of important thread locals
    this.grouperThreadLocalState.storeCurrentThreadLocals();
  }

}

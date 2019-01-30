/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import java.io.Serializable;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;


/**
 * item in the subject source cache
 */
public class SubjectSourceCacheItem implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;


  /**
   * 
   */
  public SubjectSourceCacheItem() {
  }

  /**
   * the subject
   */
  private Subject subject;
  
  /**
   * millis since 1970 last retrieved from system of record
   */
  private long lastRetrieved;
  
  /**
   * millis since 1970 last accessed from cache or system of record
   */
  private long lastAccessed;
  
  /**
   * number of times this has been accessed since last retrieved
   */
  private int numberOfTimesAccessedSinceLastRetrieved;

  /** 
   * number of times this has been retrieved from source
   */
  private int numberOfTimesRetrieved;
  
  /**
   * @return the numberOfTimesRetrieved
   */
  public int getNumberOfTimesRetrieved() {
    return this.numberOfTimesRetrieved;
  }
  
  /**
   * number of times this has been accessed since last retrieved
   * @param numberOfTimesRetrieved1 the numberOfTimesRetrieved to set
   */
  public void setNumberOfTimesRetrieved(int numberOfTimesRetrieved1) {
    this.numberOfTimesRetrieved = numberOfTimesRetrieved1;
  }

    /** 
   * number of times this has been accessed total
   */
  private int numberOfTimesAccessed;
  
  /**
   * number of times this has been accessed total
   * @return the numberOfTimesRetrieved
   */
  public int getNumberOfTimesAccessed() {
    return this.numberOfTimesAccessed;
  }
  
  /**
   * number of times this has been accessed total
   * @param numberOfTimesAccessed1 the numberOfTimesAccessed to set
   */
  public void setNumberOfTimesAccessed(int numberOfTimesAccessed1) {
    this.numberOfTimesAccessed = numberOfTimesAccessed1;
  }

  /**
   * if this is expired
   * @return true if expired
   */
  public boolean expired() {
    return expiredHelper(timeToLiveSeconds(), 
        timeToLiveNotFoundSeconds());
  }

  /**
   * @return time to live of not found
   */
  public static int timeToLiveNotFoundSeconds() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.timeToLiveNotFoundSeconds", 180);
  }

  /**
   * @return time to live seconds
   */
  public static int timeToLiveSeconds() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.timeToLiveSeconds", 3600);
  }
  
  /**
   * Resolve subjects again if necessary, after this percent of time to live
   * @return time to live seconds
   */
  public static int timeToLiveSecondsPercentageToResolveSubjectsIfNecessary() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.timeToLivePercentageToResolveSubjectsIfNecessary", 90);
  }
  
  /**
   * Resolve subjects again if necessary, after this percent of time to live for not found subjects.
   * note if the time to live for not found subjects is low, this has to be low too so there is time to resolve...
   * @return time to live seconds
   */
  public static int timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary", 50);
  }

  /**
   * if the subject has been used at least this many times in the last cycle then auto refresh the subject
   * or else just remove the subject
   * set to 0 to refresh all
   * @return the min use
   */
  public static int minUseInCycleToAutoRefresh() {
    return SubjectConfig.retrieveConfig().propertyValueInt("subject.cache.minUseInCycleToAutoRefresh", 1);
  }
  
  /**
   * 
   * @return subject needs to be resolved
   */
  public boolean needsToBeResolved() {
    return needsToBeResolvedHelper(timeToLiveSeconds(), timeToLiveSecondsPercentageToResolveSubjectsIfNecessary(),
        timeToLiveNotFoundSeconds(), timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary(), minUseInCycleToAutoRefresh());
  }
  
  /**
   * if this is needs to be resolved
   * @param timeToLiveSeconds 
   * @param timeToLiveSecondsPercentageToResolveSubjectsIfNecessary 
   * @param timeToLiveNotFoundSeconds 
   * @param timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary 
   * @param minUseInCycleToAutoRefresh 
   * @return true if expired
   */
  public boolean needsToBeResolvedHelper(int timeToLiveSeconds, int timeToLiveSecondsPercentageToResolveSubjectsIfNecessary, 
      int timeToLiveNotFoundSeconds, int timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary, int minUseInCycleToAutoRefresh) {
    
    int secondsAfterNeedsResolved = -1;

    // if no subject there is a different time to live
    if (this.subject != null) {
      secondsAfterNeedsResolved = (int)Math.round((timeToLiveSecondsPercentageToResolveSubjectsIfNecessary / 100D) * timeToLiveSeconds);
    } else {
      secondsAfterNeedsResolved = (int)Math.round((timeToLiveNotFoundPercentageToResolveSubjectsIfNecessary / 100D) * timeToLiveNotFoundSeconds);
    }

    boolean needsToBeResolved = ((System.currentTimeMillis() - this.lastRetrieved)/1000 > secondsAfterNeedsResolved) 
        && this.numberOfTimesAccessedSinceLastRetrieved >= minUseInCycleToAutoRefresh;
    return needsToBeResolved;
  }
  
  /**
   * if this is expired
   * @param timeToLiveSeconds 
   * @param timeToLiveNotFoundSeconds 
   * @return true if expired
   */
  public boolean expiredHelper(int timeToLiveSeconds, int timeToLiveNotFoundSeconds) {
    int expiresSeconds = -1;

    // if no subject there is a different time to live
    if (this.subject != null) {
      expiresSeconds = timeToLiveSeconds;
    } else {
      expiresSeconds = timeToLiveNotFoundSeconds;
    }
    
    boolean expired = ((System.currentTimeMillis() - this.lastRetrieved) / 1000) > expiresSeconds; 
    return expired;
  }

  /**
   * the subject
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }

  
  /**
   * the subject
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  
  /**
   * millis since 1970 last retrieved from system of record
   * @return the lastRetrieved
   */
  public long getLastRetrieved() {
    return this.lastRetrieved;
  }

  
  /**
   * millis since 1970 last retrieved from system of record
   * @param lastRetrieved1 the lastRetrieved to set
   */
  public void setLastRetrieved(long lastRetrieved1) {
    this.lastRetrieved = lastRetrieved1;
  }

  
  /**
   * millis since 1970 last accessed from cache or system of record
   * @return the lastAccessed
   */
  public long getLastAccessed() {
    return this.lastAccessed;
  }

  
  /**
   * millis since 1970 last accessed from cache or system of record
   * @param lastAccessed1 the lastAccessed to set
   */
  public void setLastAccessed(long lastAccessed1) {
    this.lastAccessed = lastAccessed1;
  }

  
  /**
   * number of time this has been accessed since last retrieved
   * @return the numberOfTimesAccessedSinceLastRetrieved
   */
  public int getNumberOfTimesAccessedSinceLastRetrieved() {
    return this.numberOfTimesAccessedSinceLastRetrieved;
  }

  
  /**
   * number of time this has been accessed since last retrieved
   * @param numberOfTimesAccessedSinceLastRetrieved1 the numberOfTimesAccessedSinceLastRetrieved to set
   */
  public void setNumberOfTimesAccessedSinceLastRetrieved(
      int numberOfTimesAccessedSinceLastRetrieved1) {
    this.numberOfTimesAccessedSinceLastRetrieved = numberOfTimesAccessedSinceLastRetrieved1;
  }
  
  
  
}

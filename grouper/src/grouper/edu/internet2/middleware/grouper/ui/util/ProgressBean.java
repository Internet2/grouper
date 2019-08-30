/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.util;


/**
 *
 */
public class ProgressBean {

  /**
   * if done importing
   */
  private boolean complete = false;
  /**
   * if has exception
   */
  private boolean hasException = false;
  
  /**
   * number of records complete so far
   */
  private long completeRecords;
  
  /** 
   * total records to process
   */
  private long totalRecords = -1;

  /**
   * when this was started, millis since 1970
   */
  private long startedMillis = -1;
  
  /**
   * if there are too many statuses then just quit
   */
  private int statusCountdown = 10000;

  /**
   * get elapsed seconds
   * @return seconds
   */
  public int getElapsedSeconds() {
    
    return (int)((System.currentTimeMillis() - this.startedMillis) / 1000);
  }

  /**
   * get elapsed minutes
   * @return seconds
   */
  public int getElapsedMinutes() {
    return (int)((System.currentTimeMillis() - this.startedMillis) / 1000);
  }
  
  /**
   * get the percent complete, e.g. 13
   * @return the percent complete
   */
  public int getPercentComplete() {
    
    if (this.completeRecords <= 0 || this.totalRecords <= 0) {
      return 0;
    }
    
    if (this.completeRecords >= this.totalRecords) {
      return 100;
    }
    
    int percentComplete = (int)(Math.round(100 * (this.completeRecords / this.totalRecords)));
    return percentComplete;
  }

  /**
   * 
   */
  public ProgressBean() {
  }

  /**
   * number of records complete so far
   * @return the progressCompleteRecords
   */
  public long getProgressCompleteRecords() {
    return this.completeRecords;
  }

  /**
   * total records to process (groups * subjects)
   * @return the progressTotalRecords
   */
  public long getProgressTotalRecords() {
    return this.totalRecords;
  }

  /**
   * when this was started, millis since 1970
   * @return the started
   */
  public long getStartedMillis() {
    return this.startedMillis;
  }

  /**
   * if there are too many statuses then just quit
   * @return the statusCountdown
   */
  public int getStatusCountdown() {
    return this.statusCountdown;
  }

  /**
   * if done importing
   * @return the complete
   */
  public boolean isComplete() {
    return this.complete;
  }

  /**
   * if has exception
   * @return the hasException
   */
  public boolean isHasException() {
    return this.hasException;
  }

  /**
   * if done importing
   * @param complete1 the complete to set
   */
  public void setComplete(boolean complete1) {
    this.complete = complete1;
  }

  /**
   * if has exception
   * @param hasException1 the hasException to set
   */
  public void setHasException(boolean hasException1) {
    this.hasException = hasException1;
  }

  /**
   * number of records complete so far
   * @param progressCompleteRecords1 the progressCompleteRecords to set
   */
  public void setProgressCompleteRecords(long progressCompleteRecords1) {
    this.completeRecords = progressCompleteRecords1;
  }

  /**
   * total records to process (groups * subjects)
   * @param progressTotalRecords1 the progressTotalRecords to set
   */
  public void setProgressTotalRecords(long progressTotalRecords1) {
    this.totalRecords = progressTotalRecords1;
  }

  /**
   * when this was started, millis since 1970
   * @param started1 the ssetStartedMillistartedMillis  */
  public void setStartedMillis(long started1) {
    this.startedMillis = started1;
  }

  /**
   * if there are too many statuses then just quit
   * @param statusCountdown1 the statusCountdown to set
   */
  public void setStatusCountdown(int statusCountdown1) {
    this.statusCountdown = statusCountdown1;
  }

}

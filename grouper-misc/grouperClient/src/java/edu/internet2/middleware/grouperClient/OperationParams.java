/*
 * @author mchyzer
 * $Id: OperationParams.java,v 1.2 2009-03-15 08:16:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import java.util.Map;


/**
 * params passed to an operation
 */
public class OperationParams {

  /** if the results should be saved to file */
  private boolean shouldSaveResultsToFile;
  
  /** args sent to the program */
  private Map<String, String> argMap;
  
  /** args that have been used (any left over will be considered errors */
  private Map<String, String> argMapNotUsed;

  /**
   * if the results should be saved to file
   * @return true if should be saved to file
   */
  public boolean isShouldSaveResultsToFile() {
    return this.shouldSaveResultsToFile;
  }

  /**
   * if should results be saved to file
   * @param shouldSaveResultsToFile2
   */
  public void setShouldSaveResultsToFile(boolean shouldSaveResultsToFile2) {
    this.shouldSaveResultsToFile = shouldSaveResultsToFile2;
  }

  /**
   * args passed in (without the --) and their values
   * @return the map of args passed in to their values
   */
  public Map<String, String> getArgMap() {
    return this.argMap;
  }

  /**
   * args passed in (without the --) and their values
   * @param argMap1
   */
  public void setArgMap(Map<String, String> argMap1) {
    this.argMap = argMap1;
  }

  /**
   * args still in here have not been read, so they are extra (errors)
   * @return the map
   */
  public Map<String, String> getArgMapNotUsed() {
    return this.argMapNotUsed;
  }

  /**
   * args still in here have not been read, so they are extra (errors)
   * @param argMapNotUsed1
   */
  public void setArgMapNotUsed(Map<String, String> argMapNotUsed1) {
    this.argMapNotUsed = argMapNotUsed1;
  }
  
  
  
}

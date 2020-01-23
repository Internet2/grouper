package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GcGrouperSyncLogState {

  /** if job is running */
  SUCCESS, 
  
  /**
   * if waiting for another job to finish
   */
  ERROR, 
  
  /**
   * if not running
   */
  WARNING,
  
  /**
   * if interrupted
   */
  INTERRUPTED,
  
  /**
   * if has config error
   */
  CONFIG_ERROR;
  
  /**
   * 
   * @param string
   * @return the type
   */
  public static GcGrouperSyncLogState valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcGrouperSyncLogState.class, string, false);
  }

}

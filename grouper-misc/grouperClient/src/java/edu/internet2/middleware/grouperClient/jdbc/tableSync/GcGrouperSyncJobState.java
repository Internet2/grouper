package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GcGrouperSyncJobState {

  /** if job is running */
  running, 
  
  /**
   * if waiting for another job to finish
   */
  pending, 
  
  /**
   * if not running
   */
  notRunning;
  
  /**
   * 
   * @param string
   * @return the type
   */
  public static GcGrouperSyncJobState valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcGrouperSyncJobState.class, string, false);
  }

}

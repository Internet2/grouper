package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GcGrouperSyncLogState {

  /** if job is running */
  SUCCESS(false), 
  
  /**
   * if waiting for another job to finish
   */
  ERROR(true), 
  
  /**
   * if not running
   */
  WARNING(false),
  
  /**
   * if interrupted
   */
  INTERRUPTED(true),
  
  /**
   * if has config error
   */
  CONFIG_ERROR(true),
  
  /**
   * if a failsafe issues had
   */
  ERROR_FAILSAFE(true);
  
  private GcGrouperSyncLogState(boolean theError) {
    this.error = theError;
  }
  
  private boolean error;
  
  public boolean isError() {
    return this.error;
  }
  
  /**
   * 
   * @param string
   * @return the type
   */
  public static GcGrouperSyncLogState valueOfIgnoreCase(String string) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcGrouperSyncLogState.class, string, false);
  }

}

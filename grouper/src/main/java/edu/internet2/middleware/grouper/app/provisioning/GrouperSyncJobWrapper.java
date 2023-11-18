package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;

/**
 * grouper sync job and most recent log wrapper
 */
public class GrouperSyncJobWrapper {
  
  /**
   * grouper sync job for a provisioner config
   */
  private GcGrouperSyncJob gcGrouperSyncJob;
  
  /**
   * most recent sync log for a given job id
   */
  private GcGrouperSyncLog gcGrouperSyncLog;

  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return gcGrouperSyncJob;
  }
  
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob) {
    this.gcGrouperSyncJob = gcGrouperSyncJob;
  }

  
  public GcGrouperSyncLog getGcGrouperSyncLog() {
    return gcGrouperSyncLog;
  }

  
  public void setGcGrouperSyncLog(GcGrouperSyncLog gcGrouperSyncLog) {
    this.gcGrouperSyncLog = gcGrouperSyncLog;
  }
  
}

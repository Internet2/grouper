package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class GrouperSyncLogWithOwner {
  
  private GcGrouperSyncLog gcGrouperSyncLog;
  
  private GcGrouperSyncJob gcGrouperSyncJob;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;
  
  private GcGrouperSyncMember gcGrouperSyncMember;
  
  private String logType;

  
  public GcGrouperSyncLog getGcGrouperSyncLog() {
    return gcGrouperSyncLog;
  }

  
  public void setGcGrouperSyncLog(GcGrouperSyncLog gcGrouperSyncLog) {
    this.gcGrouperSyncLog = gcGrouperSyncLog;
  }

  
  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return gcGrouperSyncJob;
  }

  
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob) {
    this.gcGrouperSyncJob = gcGrouperSyncJob;
  }

  
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }

  
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
  }

  
  public GcGrouperSyncMember getGcGrouperSyncMember() {
    return gcGrouperSyncMember;
  }

  
  public void setGcGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember) {
    this.gcGrouperSyncMember = gcGrouperSyncMember;
  }


  
  public String getLogType() {
    return logType;
  }

  
  public void setLogType(String logType) {
    this.logType = logType;
  }
  
}

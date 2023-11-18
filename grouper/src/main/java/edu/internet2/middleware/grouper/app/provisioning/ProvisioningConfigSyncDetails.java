package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

/**
 * sync details for a provisioner config
 */
public class ProvisioningConfigSyncDetails extends ProvisioningConfigSyncStats {
  
  
  /**
   * number of validation errors for a given provisioner across groups, members, and memberships
   */
  private int validationErrorCount;
  
  /**
   * number of exceptions for a given provisioner across groups, members, and memberships
   */
  private int exceptionCount;
  
  /**
   * number of target errors for a given provisioner across groups, members, and memberships
   */
  private int targetErrorCount;
  
  /**
   * list of sync jobs for a provisioner config
   */
  private List<GrouperSyncJobWrapper> syncJobs = new ArrayList<GrouperSyncJobWrapper>();
  

  
  public List<GrouperSyncJobWrapper> getSyncJobs() {
    return syncJobs;
  }

  
  public void setSyncJobs(List<GrouperSyncJobWrapper> syncJobs) {
    this.syncJobs = syncJobs;
  }


  
  public int getValidationErrorCount() {
    return validationErrorCount;
  }


  
  public void setValidationErrorCount(int validationErrorCount) {
    this.validationErrorCount = validationErrorCount;
  }


  
  public int getExceptionCount() {
    return exceptionCount;
  }


  
  public void setExceptionCount(int exceptionCount) {
    this.exceptionCount = exceptionCount;
  }


  
  public int getTargetErrorCount() {
    return targetErrorCount;
  }


  
  public void setTargetErrorCount(int targetErrorCount) {
    this.targetErrorCount = targetErrorCount;
  }
  

}




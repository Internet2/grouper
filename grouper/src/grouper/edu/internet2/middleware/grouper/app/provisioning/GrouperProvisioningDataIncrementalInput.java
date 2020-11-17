package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

/**
 * contains data to process for incremental
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataIncrementalInput {

  public GrouperProvisioningDataIncrementalInput() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * grouper uuids to retrieve without first retrieving from target
   */
  private GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc;

  /**
   * grouper uuids to retrieve from grouper and grouper sync
   * @return
   */
  public GrouperIncrementalDataToProcess getGrouperIncrementalDataToProcessWithoutRecalc() {
    if (this.grouperIncrementalDataToProcessWithoutRecalc == null) {
      this.grouperIncrementalDataToProcessWithoutRecalc = new GrouperIncrementalDataToProcess();
    }
    return grouperIncrementalDataToProcessWithoutRecalc;
  }

  /**
   * grouper uuids to retrieve from grouper and grouper sync
   * @param grouperIncrementalUuidsToRetrieveFromGrouper
   */
  public void setGrouperIncrementalDataToProcessWithoutRecalc(
      GrouperIncrementalDataToProcess grouperIncrementalUuidsToRetrieveFromGrouper) {
    this.grouperIncrementalDataToProcessWithoutRecalc = grouperIncrementalUuidsToRetrieveFromGrouper;
  }

  /**
   * grouper target objects to get from target for incremental sync
   */
  private TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest;

  /**
   * grouper uuids to retrieve with a recalc (retrieve from target and do full compare)
   */
  private GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc;
  
  /**
   * grouper uuids to retrieve with a recalc (retrieve from target and do full compare)
   * @return
   */
  public GrouperIncrementalDataToProcess getGrouperIncrementalDataToProcessWithRecalc() {
    return grouperIncrementalDataToProcessWithRecalc;
  }

  /**
   * grouper uuids to retrieve with a recalc (retrieve from target and do full compare)
   * @param grouperIncrementalDataToProcessWithRecalc
   */
  public void setGrouperIncrementalDataToProcessWithRecalc(
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc) {
    this.grouperIncrementalDataToProcessWithRecalc = grouperIncrementalDataToProcessWithRecalc;
  }


  /**
   * grouper target objects to get from target for incremental sync
   * @return target object
   */
  public TargetDaoRetrieveIncrementalDataRequest getTargetDaoRetrieveIncrementalDataRequest() {
    if (this.targetDaoRetrieveIncrementalDataRequest == null) {
      this.targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    }
    return targetDaoRetrieveIncrementalDataRequest;
  }

  /**
   * grouper target objects to get from target for incremental sync
   * @param grouperIncrementalGroupTargetObjectsToRetrieveFromTarget
   */
  public void setTargetDaoRetrieveIncrementalDataRequest(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
    this.targetDaoRetrieveIncrementalDataRequest = targetDaoRetrieveIncrementalDataRequest;
  }

  
  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  

}

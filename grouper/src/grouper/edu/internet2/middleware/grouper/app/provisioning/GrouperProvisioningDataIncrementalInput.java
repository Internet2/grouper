package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;

/**
 * contains data to process for incremental
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataIncrementalInput {

  /**
   * if we should do a full sync
   */
  private Timestamp fullSyncMessageTimestamp;
  

  /**
   * if we should do a full sync
   * @return
   */
  public Timestamp getFullSyncMessageTimestamp() {
    return fullSyncMessageTimestamp;
  }

  /**
   * if we should do a full sync
   * @param fullSyncMessageTimestamp
   */
  public void setFullSyncMessageTimestamp(Timestamp fullSyncMessageTimestamp) {
    this.fullSyncMessageTimestamp = fullSyncMessageTimestamp;
  }

  /**
   * if we should do a full sync
   */
  private boolean fullSync;
  
  /**
   * 
   * @return
   */
  public boolean isFullSync() {
    return fullSync;
  }

  /**
   * 
   * @param fullSync
   */
  public void setFullSync(boolean fullSync) {
    this.fullSync = fullSync;
  }

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
    if (this.grouperIncrementalDataToProcessWithRecalc == null) {
      this.grouperIncrementalDataToProcessWithRecalc = new GrouperIncrementalDataToProcess();
    }
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

  private List<EsbEventContainer> esbEventContainers = null;
  
  public void setEsbEventContainers(List<EsbEventContainer> esbEventContainers) {
    this.esbEventContainers = esbEventContainers;
  }
  
  public List<EsbEventContainer> getEsbEventContainers() {
    return esbEventContainers;
  }

  private List<GrouperMessage> grouperMessages;
  
  public void setGrouperMessages(List<GrouperMessage> grouperMessages) {
    this.grouperMessages = grouperMessages;
  }
  
  public List<GrouperMessage> getGrouperMessages() {
    return grouperMessages;
  }
  

}

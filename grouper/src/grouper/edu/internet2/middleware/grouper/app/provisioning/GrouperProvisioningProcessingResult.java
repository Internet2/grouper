package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLog;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class GrouperProvisioningProcessingResult {

  public GrouperProvisioningProcessingResult() {
  }

  /**
   * heartbeat thread
   */
  private GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = null;
  
  /**
   * heartbeat thread
   * @return heartbeat
   */
  public GcGrouperSyncHeartbeat getGcGrouperSyncHeartbeat() {
    return this.gcGrouperSyncHeartbeat;
  }

  /**
   * heartbeat thread
   * @param gcGrouperSyncHeartbeat1
   */
  public void setGcGrouperSyncHeartbeat(GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat1) {
    this.gcGrouperSyncHeartbeat = gcGrouperSyncHeartbeat1;
  }

  /**
   * sync log
   */
  private GcGrouperSyncLog gcGrouperSyncLog;
  
  /**
   * sync log
   * @return
   */
  public GcGrouperSyncLog getGcGrouperSyncLog() {
    return this.gcGrouperSyncLog;
  }

  /**
   * sync log
   * @param gcGrouperSyncLog1
   */
  public void setGcGrouperSyncLog(GcGrouperSyncLog gcGrouperSyncLog1) {
    this.gcGrouperSyncLog = gcGrouperSyncLog1;
  }

  /**
   * sync job
   */
  private GcGrouperSyncJob gcGrouperSyncJob;
  
  /**
   * sync job
   * @return
   */
  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return gcGrouperSyncJob;
  }

  /**
   * sync job
   * @param gcGrouperSyncJob1
   */
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob1) {
    this.gcGrouperSyncJob = gcGrouperSyncJob1;
  }

  /**
   * gc grouper sync
   */
  private GcGrouperSync gcGrouperSync;
  
  /**
   * gc grouper sync
   * @return
   */
  public GcGrouperSync getGcGrouperSync() {
    return gcGrouperSync;
  }

  /**
   * gc grouper sync
   * @param gcGrouperSync1
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync1) {
    this.gcGrouperSync = gcGrouperSync1;
  }

  /**
   * groupId to gcGrouperSyncGroup
   */
  private Map<String, GcGrouperSyncGroup> groupIdToGcGrouperSyncGroupMap;

  
  public Map<String, GcGrouperSyncGroup> getGroupIdToGcGrouperSyncGroupMap() {
    return groupIdToGcGrouperSyncGroupMap;
  }

  
  public void setGroupIdToGcGrouperSyncGroupMap(
      Map<String, GcGrouperSyncGroup> groupIdToGcGrouperSyncGroup) {
    this.groupIdToGcGrouperSyncGroupMap = groupIdToGcGrouperSyncGroup;
  }
  
  private Map<String, Group> groupIdGroupMap;

  public Map<String, Group> getGroupIdGroupMap() {
    return groupIdGroupMap;
  }


  
  public void setGroupIdGroupMap(Map<String, Group> groupIdGroupMap) {
    this.groupIdGroupMap = groupIdGroupMap;
  }
  
  private List<String> groupIdsToAddToTarget;


  
  public List<String> getGroupIdsToAddToTarget() {
    return groupIdsToAddToTarget;
  }


  
  public void setGroupIdsToAddToTarget(List<String> groupIdsToAddToTarget) {
    this.groupIdsToAddToTarget = groupIdsToAddToTarget;
  }
  
  private List<String> groupIdsToRemoveFromTarget;

  /**
   * memberId to gcGrouperSyncMember.  Note: only the ones found are there...  not all
   */
  private Map<String, GcGrouperSyncMember> memberIdToGcGrouperSyncMemberMap;


  
  /**
   * memberId to gcGrouperSyncMember.  Note: only the ones found are there...  not all
   * @return map
   */
  public Map<String, GcGrouperSyncMember> getMemberIdToGcGrouperSyncMemberMap() {
    return this.memberIdToGcGrouperSyncMemberMap;
  }

  /**
   * memberId to gcGrouperSyncMember.  Note: only the ones found are there...  not all
   * @param memberIdToGcGrouperSyncMemberMap1
   */
  public void setMemberIdToGcGrouperSyncMemberMap(
      Map<String, GcGrouperSyncMember> memberIdToGcGrouperSyncMemberMap1) {
    this.memberIdToGcGrouperSyncMemberMap = memberIdToGcGrouperSyncMemberMap1;
  }

  public List<String> getGroupIdsToRemoveFromTarget() {
    return groupIdsToRemoveFromTarget;
  }


  
  public void setGroupIdsToRemoveFromTarget(List<String> groupIdsToRemoveFromTarget) {
    this.groupIdsToRemoveFromTarget = groupIdsToRemoveFromTarget;
  }
  
  
  
  
}

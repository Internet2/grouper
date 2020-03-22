package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;

/**
 * message sent in JSON to tell the provisioner to analyze objects
 * @author mchyzer
 *
 */
public class ProvisioningMessage {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    ProvisioningMessage provisioningMessage = new ProvisioningMessage();
    provisioningMessage.setFullSync(true);
    provisioningMessage.setFullSyncType("optionalFullSyncType");

//    provisioningMessage.setGroupIdsForSync(new String[] {"abc123", "def456"});
//   provisioningMessage.setMemberIdsForSync(new String[] {"abc123", "def456"});
    
//    provisioningMessage.setMembershipsForSync(new ProvisioningMembershipMessage[] {
//        new ProvisioningMembershipMessage("abc123", "jkl789"),
//        new ProvisioningMembershipMessage("def456", "qwe543")
//    });
    
    String message = provisioningMessage.toJson();
    System.out.println(message);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperMessagingEngine.send(
        new GrouperMessageSendParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
          .assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName("grouperProvisioningControl_myPspngProvisioner")
          .assignAutocreateObjects(true)
          .addMessageBody(message));
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  public ProvisioningMessage() {
  }

  /**
   * blocking will do a proper sync and block other jobs (e.g. change log consumer).
   * non blocking will do a compare, and send messaging events about the output
   */
  private Boolean blocking;
  
  /**
   * blocking will do a proper sync and block other jobs (e.g. change log consumer).
   * non blocking will do a compare, and send messaging events about the output
   * @return if blocking
   */
  public Boolean getBlocking() {
    return this.blocking;
  }

  /**
   * blocking will do a proper sync and block other jobs (e.g. change log consumer).
   * non blocking will do a compare, and send messaging events about the output
   * @param blocking1
   */
  public void setBlocking(Boolean blocking1) {
    this.blocking = blocking1;
  }

  /**
   * if a full sync should be run
   */
  private Boolean fullSync = null;

  /**
   * if a full sync should be run
   * @return true if should run a full sync
   */
  public Boolean getFullSync() {
    return this.fullSync;
  }

  /**
   * if a full sync should be run
   * @param fullSync1
   */
  public void setFullSync(Boolean fullSync1) {
    this.fullSync = fullSync1;
  }

  /**
   * full sync type
   */
  private String fullSyncType;
  
  
  
  /**
   * full sync type
   * @return
   */
  public String getFullSyncType() {
    return this.fullSyncType;
  }

  /**
   * full sync type
   * @param fullSyncType1
   */
  public void setFullSyncType(String fullSyncType1) {
    this.fullSyncType = fullSyncType1;
  }

  /**
   * group ids to do group syncs for
   */
  private String[] groupIdsForSync;

  /**
   * group ids to do group syncs for
   * @return group ids to sync
   */
  public String[] getGroupIdsForSync() {
    return this.groupIdsForSync;
  }

  /**
   * group ids to do group syncs for
   * @param groupIdsForSync1
   */
  public void setGroupIdsForSync(String[] groupIdsForSync1) {
    this.groupIdsForSync = groupIdsForSync1;
  }
  
  /**
   * member ids to do member syncs for
   */
  private String[] memberIdsForSync;

  /**
   * member ids to do member syncs for
   * @return member ids
   */
  public String[] getMemberIdsForSync() {
    return this.memberIdsForSync;
  }

  /**
   * member ids to do member syncs for
   * @param memberIdsForSync1
   */
  public void setMemberIdsForSync(String[] memberIdsForSync1) {
    this.memberIdsForSync = memberIdsForSync1;
  }

  /**
   * memberships to analyze for sync
   */
  private ProvisioningMembershipMessage[] membershipsForSync;

  /**
   * memberships to analyze for sync
   * @return memberships
   */
  public ProvisioningMembershipMessage[] getMembershipsForSync() {
    return this.membershipsForSync;
  }

  /**
   * memberships to analyze for sync
   * @param membershipsForSync1
   */
  public void setMembershipsForSync(ProvisioningMembershipMessage[] membershipsForSync1) {
    this.membershipsForSync = membershipsForSync1;
  }

  /**
   * convert to json
   * @return the json of this
   */
  public String toJson() {
     String jsonString = GrouperUtil.jsonConvertToNoWrap(this);
     return jsonString;
  }

  /**
   * convert from json
   * @param json
   * @return the object of this json
   */
  public static ProvisioningMessage fromJson(String json) {
    ProvisioningMessage provisioningMessage = GrouperUtil.jsonConvertFrom(json, ProvisioningMessage.class);
    return provisioningMessage;
  }
  
}

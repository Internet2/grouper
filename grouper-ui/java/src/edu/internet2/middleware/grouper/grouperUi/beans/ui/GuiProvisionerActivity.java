package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GuiProvisionerActivity {
  
  private String type;
 
  private String action;
  
  private String name;
  
  private Boolean provisionable;
  
  private Boolean inTarget;
  
  private Boolean inTargetInsertOrExists;
  
  private Timestamp inTargetStart;
  
  private Timestamp inTargetEnd;
  
  private Timestamp provisionableStart;
  
  private Timestamp provisionableEnd;
  
  private Timestamp lastUpdated;
  
  //only for groups and entities
  private Timestamp lastObjectSyncStart;
  
  /**
   * when this object was last synced. only for groups and entities
   */
  private Timestamp lastObjectSync;
  
  /**
   * when this groups name and description and metadata was synced, start. only for groups and entities
   */
  private Timestamp lastObjectMetadataSyncStart;
  
  /**
   * when this groups name and description and metadata was synced. only for groups and entities
   */
  private Timestamp lastObjectMetadataSync;
  
  //only for groups and entities
  private String objectAttributeValueCache0;
  
  //only for groups and entities
  private String objectAttributeValueCache1;
  
  //only for groups and entities
  private String objectAttributeValueCache2;
  
  //only for groups and entities
  private String objectAttributeValueCache3;
  
  /**
   * when metadata was last updated
   */
  private Timestamp metadataUpdated;
  
  /**
   * if the last sync had an error, this is the error message
   */
  private String errorMessage; 

  /**
   * this the last sync had an error, this was the error timestamp
   */
  private Timestamp errorTimestamp;
  
  private GcGrouperSyncErrorCode errorCode;
  
  /**
   * last time a record was processed
   */
  private Timestamp lastTimeWorkWasDone;
  
  
  public static List<GuiProvisionerActivity> convertFromGcGrouperSyncObjects(List<GcGrouperSyncGroup> grouperSyncGroups, 
      List<GcGrouperSyncMember> grouperSyncMembers, List<GcGrouperSyncMembership> grouperSyncMemberships) {
    
    List<GuiProvisionerActivity> result = new ArrayList<GuiProvisionerActivity>();
    
    String insertAction = TextContainer.textOrNull("provisionerActivityActionInsert");
    String deleteAction = TextContainer.textOrNull("provisionerActivityActionDelete");
    String existsAction = TextContainer.textOrNull("provisionerActivityActionExists");
    
    for (GcGrouperSyncGroup grouperSyncGroup: GrouperUtil.nonNull(grouperSyncGroups)) {
      GuiProvisionerActivity guiProvisionerActivity = new GuiProvisionerActivity();
      
      String group = TextContainer.textOrNull("provisionerConfigObjectTypeGroup");
      guiProvisionerActivity.setType(group);
      guiProvisionerActivity.setName(grouperSyncGroup.getGroupName());
      guiProvisionerActivity.setProvisionable(grouperSyncGroup.isProvisionable());
      guiProvisionerActivity.setInTarget(grouperSyncGroup.getInTarget());
      guiProvisionerActivity.setInTargetInsertOrExists(grouperSyncGroup.isInTargetInsertOrExists()); // header is Insert, is that correct?
      guiProvisionerActivity.setInTargetStart(grouperSyncGroup.getInTargetStart());
      guiProvisionerActivity.setInTargetEnd(grouperSyncGroup.getInTargetEnd());
      guiProvisionerActivity.setProvisionableStart(grouperSyncGroup.getProvisionableStart());
      guiProvisionerActivity.setProvisionableEnd(grouperSyncGroup.getProvisionableEnd());
      guiProvisionerActivity.setLastUpdated(grouperSyncGroup.getLastUpdated());
      guiProvisionerActivity.setLastObjectSyncStart(grouperSyncGroup.getLastGroupSyncStart());
      guiProvisionerActivity.setLastObjectMetadataSyncStart(grouperSyncGroup.getLastGroupMetadataSyncStart());
      guiProvisionerActivity.setLastObjectMetadataSync(grouperSyncGroup.getLastGroupMetadataSync());
      guiProvisionerActivity.setObjectAttributeValueCache0(grouperSyncGroup.getGroupAttributeValueCache0());
      guiProvisionerActivity.setObjectAttributeValueCache1(grouperSyncGroup.getGroupAttributeValueCache1());
      guiProvisionerActivity.setObjectAttributeValueCache2(grouperSyncGroup.getGroupAttributeValueCache2());
      guiProvisionerActivity.setObjectAttributeValueCache3(grouperSyncGroup.getGroupAttributeValueCache3());
      guiProvisionerActivity.setMetadataUpdated(grouperSyncGroup.getMetadataUpdated());
      guiProvisionerActivity.setErrorMessage(grouperSyncGroup.getErrorMessage());
      guiProvisionerActivity.setErrorTimestamp(grouperSyncGroup.getErrorTimestamp());
      guiProvisionerActivity.setErrorCode(grouperSyncGroup.getErrorCode());
      guiProvisionerActivity.setLastTimeWorkWasDone(grouperSyncGroup.getLastTimeWorkWasDone());
      
      
      int comparisonResult = GrouperUtil.compare(grouperSyncGroup.getInTargetStart(), grouperSyncGroup.getInTargetEnd());
      
      if (comparisonResult > 0) {
        if (grouperSyncGroup.isInTargetInsertOrExists()) {
          guiProvisionerActivity.setAction(insertAction);
        } else {
          guiProvisionerActivity.setAction(existsAction);
        }
        
      } else {
        guiProvisionerActivity.setAction(deleteAction);
      }
      
      result.add(guiProvisionerActivity);
    }
    
    for (GcGrouperSyncMember grouperSyncMember: GrouperUtil.nonNull(grouperSyncMembers)) {
      GuiProvisionerActivity guiProvisionerActivity = new GuiProvisionerActivity();
      
      String entity = TextContainer.textOrNull("provisionerConfigObjectTypeEntity");
      guiProvisionerActivity.setType(entity);
      
      guiProvisionerActivity.setName(grouperSyncMember.getSourceId() + " - "+grouperSyncMember.getSubjectId());
      guiProvisionerActivity.setProvisionable(grouperSyncMember.isProvisionable());
      guiProvisionerActivity.setInTarget(grouperSyncMember.getInTarget());
      guiProvisionerActivity.setInTargetInsertOrExists(grouperSyncMember.isInTargetInsertOrExists()); // header is Insert, is that correct?
      guiProvisionerActivity.setInTargetStart(grouperSyncMember.getInTargetStart());
      guiProvisionerActivity.setInTargetEnd(grouperSyncMember.getInTargetEnd());
      guiProvisionerActivity.setProvisionableStart(grouperSyncMember.getProvisionableStart());
      guiProvisionerActivity.setProvisionableEnd(grouperSyncMember.getProvisionableEnd());
      guiProvisionerActivity.setLastUpdated(grouperSyncMember.getLastUpdated());
      guiProvisionerActivity.setLastObjectSyncStart(grouperSyncMember.getLastUserSyncStart());
      guiProvisionerActivity.setLastObjectMetadataSyncStart(grouperSyncMember.getLastUserMetadataSyncStart());
      guiProvisionerActivity.setLastObjectMetadataSync(grouperSyncMember.getLastUserMetadataSync());
      guiProvisionerActivity.setObjectAttributeValueCache0(grouperSyncMember.getEntityAttributeValueCache0());
      guiProvisionerActivity.setObjectAttributeValueCache1(grouperSyncMember.getEntityAttributeValueCache1());
      guiProvisionerActivity.setObjectAttributeValueCache2(grouperSyncMember.getEntityAttributeValueCache2());
      guiProvisionerActivity.setObjectAttributeValueCache3(grouperSyncMember.getEntityAttributeValueCache3());
      guiProvisionerActivity.setMetadataUpdated(grouperSyncMember.getMetadataUpdated());
      guiProvisionerActivity.setErrorMessage(grouperSyncMember.getErrorMessage());
      guiProvisionerActivity.setErrorTimestamp(grouperSyncMember.getErrorTimestamp());
      guiProvisionerActivity.setErrorCode(grouperSyncMember.getErrorCode());
      guiProvisionerActivity.setLastTimeWorkWasDone(grouperSyncMember.getLastTimeWorkWasDone());
      
      int comparisonResult = GrouperUtil.compare(grouperSyncMember.getInTargetStart(), grouperSyncMember.getInTargetEnd());
      
      if (comparisonResult > 0) {
        if (grouperSyncMember.isInTargetInsertOrExists()) {
          guiProvisionerActivity.setAction(insertAction);
        } else {
          guiProvisionerActivity.setAction(existsAction);
        }
        
      } else {
        guiProvisionerActivity.setAction(deleteAction);
      }
      
      result.add(guiProvisionerActivity);
      
    }
    
    for (GcGrouperSyncMembership grouperSyncMembership: GrouperUtil.nonNull(grouperSyncMemberships)) {
      GuiProvisionerActivity guiProvisionerActivity = new GuiProvisionerActivity();
      
      String mship = TextContainer.textOrNull("provisionerConfigObjectTypeMship");
      guiProvisionerActivity.setType(mship);
      
      guiProvisionerActivity.setName(grouperSyncMembership.getGrouperSyncGroup().getGroupName() + " - "+ grouperSyncMembership.getGrouperSyncMember().getSubjectId());
      guiProvisionerActivity.setInTarget(grouperSyncMembership.getInTarget());
      guiProvisionerActivity.setInTargetInsertOrExists(grouperSyncMembership.isInTargetInsertOrExists());
      guiProvisionerActivity.setInTargetStart(grouperSyncMembership.getInTargetStart());
      guiProvisionerActivity.setInTargetEnd(grouperSyncMembership.getInTargetEnd());
      guiProvisionerActivity.setLastUpdated(grouperSyncMembership.getLastUpdated());
      guiProvisionerActivity.setMetadataUpdated(grouperSyncMembership.getMetadataUpdated());
      guiProvisionerActivity.setErrorMessage(grouperSyncMembership.getErrorMessage());
      guiProvisionerActivity.setErrorTimestamp(grouperSyncMembership.getErrorTimestamp());
      guiProvisionerActivity.setErrorCode(grouperSyncMembership.getErrorCode());
      
      int comparisonResult = GrouperUtil.compare(grouperSyncMembership.getInTargetStart(), grouperSyncMembership.getInTargetEnd());
      
      if (comparisonResult > 0) {
        if (grouperSyncMembership.isInTargetInsertOrExists()) {
          guiProvisionerActivity.setAction(insertAction);
        } else {
          guiProvisionerActivity.setAction(existsAction);
        }
        
      } else {
        guiProvisionerActivity.setAction(deleteAction);
      }
      
      result.add(guiProvisionerActivity);
    }
    
    Collections.sort(result, new Comparator<GuiProvisionerActivity>() {
      @Override
      public int compare(GuiProvisionerActivity obj1, GuiProvisionerActivity obj2) {
          return obj2.getLastUpdated().compareTo(obj1.getLastUpdated());
      }
    });
    
    return result;
  }
  
  
  public String getType() {
    return type;
  }

  
  public void setType(String type) {
    this.type = type;
  }

  
  public String getAction() {
    return action;
  }

  
  public void setAction(String action) {
    this.action = action;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public void setProvisionable(Boolean provisionable) {
    this.provisionable = provisionable;
  }

  
  public void setInTarget(Boolean inTarget) {
    this.inTarget = inTarget;
  }

  
  public void setInTargetInsertOrExists(Boolean inTargetInsertOrExists) {
    this.inTargetInsertOrExists = inTargetInsertOrExists;
  }


  public Timestamp getInTargetStart() {
    return inTargetStart;
  }

  
  public void setInTargetStart(Timestamp inTargetStart) {
    this.inTargetStart = inTargetStart;
  }

  
  public Timestamp getInTargetEnd() {
    return inTargetEnd;
  }

  
  public void setInTargetEnd(Timestamp inTargetEnd) {
    this.inTargetEnd = inTargetEnd;
  }

  
  public Timestamp getProvisionableStart() {
    return provisionableStart;
  }

  
  public void setProvisionableStart(Timestamp provisionableStart) {
    this.provisionableStart = provisionableStart;
  }

  
  public Timestamp getProvisionableEnd() {
    return provisionableEnd;
  }

  
  public void setProvisionableEnd(Timestamp provisionableEnd) {
    this.provisionableEnd = provisionableEnd;
  }

  
  public Timestamp getLastUpdated() {
    return lastUpdated;
  }

  
  public void setLastUpdated(Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  
  public Timestamp getLastObjectSyncStart() {
    return lastObjectSyncStart;
  }

  
  public void setLastObjectSyncStart(Timestamp lastObjectSyncStart) {
    this.lastObjectSyncStart = lastObjectSyncStart;
  }

  
  public Timestamp getLastObjectSync() {
    return lastObjectSync;
  }

  
  public void setLastObjectSync(Timestamp lastObjectSync) {
    this.lastObjectSync = lastObjectSync;
  }

  
  public Timestamp getLastObjectMetadataSyncStart() {
    return lastObjectMetadataSyncStart;
  }

  
  public void setLastObjectMetadataSyncStart(Timestamp lastObjectMetadataSyncStart) {
    this.lastObjectMetadataSyncStart = lastObjectMetadataSyncStart;
  }

  
  public Timestamp getLastObjectMetadataSync() {
    return lastObjectMetadataSync;
  }

  
  public void setLastObjectMetadataSync(Timestamp lastObjectMetadataSync) {
    this.lastObjectMetadataSync = lastObjectMetadataSync;
  }

  
  public String getObjectAttributeValueCache0() {
    return objectAttributeValueCache0;
  }

  
  public void setObjectAttributeValueCache0(String objectAttributeValueCache0) {
    this.objectAttributeValueCache0 = objectAttributeValueCache0;
  }

  
  public String getObjectAttributeValueCache1() {
    return objectAttributeValueCache1;
  }

  
  public void setObjectAttributeValueCache1(String objectAttributeValueCache1) {
    this.objectAttributeValueCache1 = objectAttributeValueCache1;
  }

  
  public String getObjectAttributeValueCache2() {
    return objectAttributeValueCache2;
  }

  
  public void setObjectAttributeValueCache2(String objectAttributeValueCache2) {
    this.objectAttributeValueCache2 = objectAttributeValueCache2;
  }

  
  public String getObjectAttributeValueCache3() {
    return objectAttributeValueCache3;
  }

  
  public void setObjectAttributeValueCache3(String objectAttributeValueCache3) {
    this.objectAttributeValueCache3 = objectAttributeValueCache3;
  }

  
  public Timestamp getMetadataUpdated() {
    return metadataUpdated;
  }

  
  public void setMetadataUpdated(Timestamp metadataUpdated) {
    this.metadataUpdated = metadataUpdated;
  }

  
  public String getErrorMessage() {
    return errorMessage;
  }

  
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  
  public Timestamp getErrorTimestamp() {
    return errorTimestamp;
  }

  
  public void setErrorTimestamp(Timestamp errorTimestamp) {
    this.errorTimestamp = errorTimestamp;
  }

  
  public GcGrouperSyncErrorCode getErrorCode() {
    return errorCode;
  }

  
  public void setErrorCode(GcGrouperSyncErrorCode errorCode) {
    this.errorCode = errorCode;
  }


  public Timestamp getLastTimeWorkWasDone() {
    return lastTimeWorkWasDone;
  }

  
  public void setLastTimeWorkWasDone(Timestamp lastTimeWorkWasDone) {
    this.lastTimeWorkWasDone = lastTimeWorkWasDone;
  }


  
  public Boolean getProvisionable() {
    return provisionable;
  }


  
  public Boolean getInTarget() {
    return inTarget;
  }


  
  public Boolean getInTargetInsertOrExists() {
    return inTargetInsertOrExists;
  }
  
}

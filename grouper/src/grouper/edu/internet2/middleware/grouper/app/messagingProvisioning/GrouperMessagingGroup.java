package edu.internet2.middleware.grouper.app.messagingProvisioning;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperMessagingGroup {
  
  /**
   * { 
  "encrypted":false,
  "esbEvent":[ 
    { 
      "changeOccurred":false,
      "createdOnMicros":1502133203662000,
      "displayName":"etc:sysadminReadonly",
      "eventType":"GROUP_ADD",
      "id":"ae0dde273bd0472c8257f36ffccf20ad",
      "name":"etc:sysadminReadonly",
      "parentStemId":"4fd44656ad6f423eaaddbd896fdc1aaa",
      "sequenceNumber":"793"
    }
  ]
}
   */
  
  
  private String displayName;
  
  private String id;
  
  private String groupId;
  
  private String groupName;
  
  private String displayExtension;
  
  private String description;
  
  private String name;
  
  private String parentStemId;
  

  
  public String getDisplayName() {
    return displayName;
  }

  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public String getParentStemId() {
    return parentStemId;
  }

  
  public void setParentStemId(String parentStemId) {
    this.parentStemId = parentStemId;
  }

  
  
  public String getGroupId() {
    return groupId;
  }


  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }


  
  public String getGroupName() {
    return groupName;
  }


  
  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }


  
  public String getDisplayExtension() {
    return displayExtension;
  }


  
  public void setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
  }


  
  public String getDescription() {
    return description;
  }


  
  public void setDescription(String description) {
    this.description = description;
  }


  public static GrouperMessagingGroup fromProvisioningGroup(ProvisioningGroup targetGroup) {
    
    GrouperMessagingGroup grouperMessagingGroup = new GrouperMessagingGroup();
    grouperMessagingGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    grouperMessagingGroup.setDisplayExtension(GrouperUtil.extensionFromName(targetGroup.getDisplayName()));
    grouperMessagingGroup.setDisplayName(targetGroup.getDisplayName());
    grouperMessagingGroup.setGroupId(targetGroup.getId());
    grouperMessagingGroup.setGroupName(targetGroup.getName());
    grouperMessagingGroup.setId(targetGroup.getId());
    grouperMessagingGroup.setName(targetGroup.getName());
    grouperMessagingGroup.setParentStemId(null); //TODO populate it later
    
    return grouperMessagingGroup;
    
  }
  
  public ObjectNode toJson(GrouperMessagingConfiguration grouperMessagingConfiguration) {
    return grouperMessagingConfiguration.getMessagingFormatType().toGroupJson(this);
  }


}

package edu.internet2.middleware.grouper.app.messagingProvisioning;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningMembershipFieldType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class GrouperMessagingMembership {

  /**
   *  { 
  "encrypted":false, // hardcode to false
  "esbEvent":[ 
    { 
      "changeOccurred":false, // make it true for everything hardcode
      "createdOnMicros":1502100000596000,
      "eventType":"MEMBERSHIP_ADD",
      "fieldName":"members",
      "groupId":"ccf74f3b4d0743428f7d72a14d8d81db",
      "groupName":"test:testLoader",
      "id":"484190ca24e54ea7a6ac9e7d26089afa",
      "membershipType":"flattened",
      "sequenceNumber":"790",
      "sourceId":"jdbc",
      "subjectId":"test.subject.2"
    }
  ]
}
   */
  
  
  private String fieldName;
  
  private String fieldId;
  
  private String groupId;
  
  private String groupName;
  
  private String id;
  
  private String membershipType;
  
  private String sourceId;
  
  private String subjectId;

  private String memberId;
  
  
  public String getFieldName() {
    return fieldName;
  }

  
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
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

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getMembershipType() {
    return membershipType;
  }

  
  public void setMembershipType(String membershipType) {
    this.membershipType = membershipType;
  }

  
  
  public String getSourceId() {
    return sourceId;
  }

  
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  
  public String getSubjectId() {
    return subjectId;
  }

  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }


  
  public String getFieldId() {
    return fieldId;
  }


  
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }


  
  public String getMemberId() {
    return memberId;
  }


  
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
  
  public static GrouperMessagingMembership fromProvisioningMembership(ProvisioningMembership targetMembership) {
    
    GrouperProvisioningConfiguration messagingConfiguration = targetMembership.getProvisioningMembershipWrapper()
        .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType = messagingConfiguration.getGrouperProvisioningMembershipFieldType();
    
    GrouperMessagingMembership grouperMessagingMembership = new GrouperMessagingMembership();
    grouperMessagingMembership.setFieldId(grouperProvisioningMembershipFieldType.getFieldId());
    grouperMessagingMembership.setFieldName(grouperProvisioningMembershipFieldType.getFieldName());
    grouperMessagingMembership.setGroupId(targetMembership.getProvisioningGroupId());
    grouperMessagingMembership.setGroupName(targetMembership.getProvisioningGroup().getName());
    grouperMessagingMembership.setId(targetMembership.getId());
    grouperMessagingMembership.setMemberId(targetMembership.getProvisioningEntityId());
    grouperMessagingMembership.setMembershipType("flattened"); 
    grouperMessagingMembership.setSourceId(targetMembership.getProvisioningEntity().retrieveAttributeValueString("subjectSourceId"));
    grouperMessagingMembership.setSubjectId(targetMembership.getProvisioningEntity().getSubjectId());
    
    return grouperMessagingMembership;
  
  }
  
  public ObjectNode toJson(GrouperMessagingConfiguration grouperMessagingConfiguration) {
    return grouperMessagingConfiguration.getMessagingFormatType().toMembershipJson(this);
  }
  
}

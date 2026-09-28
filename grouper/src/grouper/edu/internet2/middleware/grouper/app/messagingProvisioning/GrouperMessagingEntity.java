package edu.internet2.middleware.grouper.app.messagingProvisioning;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class GrouperMessagingEntity {
  
  private String id;
  
  private String subjectId;
  
  private String subjectSourceId;

  private String subjectIdentifier0;
  
  /**
   * the provisioning entity this was created from, for format scripts
   */
  private ProvisioningEntity provisioningEntity;
  
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  
  public String getSubjectId() {
    return subjectId;
  }

  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }

  
  public String getSubjectIdentifier0() {
    return subjectIdentifier0;
  }

  
  public void setSubjectIdentifier0(String subjectIdentifier0) {
    this.subjectIdentifier0 = subjectIdentifier0;
  }

  public ProvisioningEntity getProvisioningEntity() {
    return provisioningEntity;
  }

  public void setProvisioningEntity(ProvisioningEntity provisioningEntity) {
    this.provisioningEntity = provisioningEntity;
  }

  public static GrouperMessagingEntity fromProvisioningEntity(ProvisioningEntity targetEntity) {
   
    GrouperMessagingEntity grouperMessagingEntity = new GrouperMessagingEntity();
    grouperMessagingEntity.setProvisioningEntity(targetEntity);
    grouperMessagingEntity.setId(targetEntity.getId());
    grouperMessagingEntity.setSubjectId(targetEntity.getSubjectId());
    grouperMessagingEntity.setSubjectSourceId(targetEntity.retrieveAttributeValueString("subjectSourceId"));
    grouperMessagingEntity.setSubjectIdentifier0(targetEntity.retrieveAttributeValueString("subjectIdentifier0"));
    
    return grouperMessagingEntity;
    
  }
  
  /**
   * @param grouperMessagingConfiguration
   * @param eventType the event type being sent, e.g. GROUP_ADD, available to format scripts
   * @return the message body
   */
  public ObjectNode toJson(GrouperMessagingConfiguration grouperMessagingConfiguration, String eventType) {
    return grouperMessagingConfiguration.getMessagingFormatType().toEntityJson(grouperMessagingConfiguration, this, eventType);
  }

}

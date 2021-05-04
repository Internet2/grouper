package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shilen
 */
public class GrouperProvisioningObjectAttributes {

  private String id;
  private String name;
  private Long idIndex;
  private String markerAttributeAssignId;
  
  private String provisioningTarget;
  private String provisioningDirectAssign;
  private String provisioningDoProvision;
  private String provisioningOwnerStemId;
  private String provisioningMetadataJson;
  private String provisioningStemScope;
  private boolean isOwnedByGroup;
  private boolean isOwnedByStem;
  
  private Map<String, Object> metadataNameValues = null;
  
  public GrouperProvisioningObjectAttributes(String id, String name, Long idIndex, String markerAttributeAssignId) {
    this.id = id;
    this.name = name;
    this.idIndex = idIndex;
    this.markerAttributeAssignId = markerAttributeAssignId;
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
  
  public String getProvisioningTarget() {
    return provisioningTarget;
  }
  
  public void setProvisioningTarget(String provisioningTarget) {
    this.provisioningTarget = provisioningTarget;
  }
  
  public String getProvisioningDirectAssign() {
    return provisioningDirectAssign;
  }
  
  public void setProvisioningDirectAssign(String provisioningDirectAssign) {
    this.provisioningDirectAssign = provisioningDirectAssign;
  }
  
  public String getProvisioningDoProvision() {
    return provisioningDoProvision;
  }
  
  public void setProvisioningDoProvision(String provisioningDoProvision) {
    this.provisioningDoProvision = provisioningDoProvision;
  }
  
  public String getProvisioningOwnerStemId() {
    return provisioningOwnerStemId;
  }
  
  public void setProvisioningOwnerStemId(String provisioningOwnerStemId) {
    this.provisioningOwnerStemId = provisioningOwnerStemId;
  }
  
  public String getProvisioningMetadataJson() {
    return provisioningMetadataJson;
  }
  
  public void setProvisioningMetadataJson(String provisioningMetadataJson) {
    this.provisioningMetadataJson = provisioningMetadataJson;
  }
  
  public String getProvisioningStemScope() {
    return provisioningStemScope;
  }
  
  public void setProvisioningStemScope(String provisioningStemScope) {
    this.provisioningStemScope = provisioningStemScope;
  }
  
  
  public String getMarkerAttributeAssignId() {
    return markerAttributeAssignId;
  }

  
  public void setMarkerAttributeAssignId(String markerAttributeAssignId) {
    this.markerAttributeAssignId = markerAttributeAssignId;
  }

  public boolean isOwnedByGroup() {
    return isOwnedByGroup;
  }

  
  public void setOwnedByGroup(boolean isOwnedByGroup) {
    this.isOwnedByGroup = isOwnedByGroup;
  }

  
  public boolean isOwnedByStem() {
    return isOwnedByStem;
  }

  
  public void setOwnedByStem(boolean isOwnedByStem) {
    this.isOwnedByStem = isOwnedByStem;
  }
  
  
  public Long getIdIndex() {
    return idIndex;
  }

  
  public void setIdIndex(Long idIndex) {
    this.idIndex = idIndex;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getMetadataNameValues() {
    if (this.provisioningMetadataJson == null) {
      return new HashMap<String, Object>();
    }

    if (metadataNameValues == null) {
      try {
        this.metadataNameValues = GrouperProvisioningSettings.objectMapper.readValue(provisioningMetadataJson, Map.class);
      } catch(Exception e) {
        throw new RuntimeException("could not convert json string " + provisioningMetadataJson + " to Map object", e);
      }
    }
    
    return metadataNameValues;
  }
}

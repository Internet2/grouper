package edu.internet2.middleware.grouper.app.provisioning;

/**
 * @author shilen
 */
public class GrouperProvisioningObjectAttributes {

  private String id;
  private String name;
  
  private String provisioningTarget;
  private String provisioningDirectAssign;
  private String provisioningDoProvision;
  private String provisioningOwnerStemId;
  private String provisioningMetadataJson;
  private String provisioningStemScope;
  private boolean isOwnedByGroup;
  private boolean isOwnedByStem;
  
  
  public GrouperProvisioningObjectAttributes(String id, String name) {
    this.id = id;
    this.name = name;
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
}

package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Stem.Scope;

public class GrouperProvisioningAttributeValue {
  
  /**
   * provisioning target name
   */
  private String targetName;
  
  /**
   * is direct assignment
   */
  private boolean directAssignment;
  
  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   */
  private String ownerStemId;
  
  /**
   * stem scope (one|sub)
   */
  private String stemScopeString;
  
  /**
   * provisioning target name
   * should provision if not null
   */
  private String doProvision;
  
  /**
   * millis since last full provision
   */
  private String lastFullMillisSince1970String;
  
  /**
   * millis since last incremental provision
   */
  private String lastIncrementalMillisSince1970String;

  /**
   * last full summary
   */
  private String lastFullSummary;
  
  /**
   * last incremental summary
   */
  private String lastIncrementalSummary;
  
  
  /**
   * provisioning object metadata
   */
  // private GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata;
  
  private Map<String, Object> metadataNameValues = new HashMap<String, Object>();

  /**
   * provisioning target name
   * @return targetName
   */
  public String getTargetName() {
    return targetName;
  }

  /**
   * provisioning target name
   * @param targetName
   */
  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  /**
   * is direct assignment
   * @return
   */
  public boolean isDirectAssignment() {
    return directAssignment;
  }

  /**
   * is direct assignment
   * @param directAssignment
   */
  public void setDirectAssignment(boolean directAssignment) {
    this.directAssignment = directAssignment;
  }
  
  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   * @return
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  /**
   * owner stem id where type config is inherited from. This should be populated only when it's not direct assignment
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
  }

  /**
   * stem scope (one|sub)
   * @return
   */
  public String getStemScopeString() {
    return stemScopeString;
  }
  
  /**
   * stem scope (one|sub)
   * @param stemScopeString
   */
  public void setStemScopeString(String stemScopeString) {
    this.stemScopeString = stemScopeString;
  }

  /**
   * provisioning target name
   * should provision if not null
   * @return
   */
  public String getDoProvision() {
    return doProvision;
  }
  
  /**
   * @return true to provision if doProvision is not null
   */
  public boolean isDoProvision() {
    return doProvision != null;
  }

  /**
   * provisioning target name
   * should provision if not null
   * @param doProvision
   */
  public void setDoProvision(String doProvision) {
    this.doProvision = doProvision;
  }

  /**
   * millis since last full provision
   * @return
   */
  public String getLastFullMillisSince1970String() {
    return lastFullMillisSince1970String;
  }

  /**
   * millis since last full provision
   * @param lastFullMillisSince1970String
   */
  public void setLastFullMillisSince1970String(String lastFullMillisSince1970String) {
    this.lastFullMillisSince1970String = lastFullMillisSince1970String;
  }

  /**
   * millis since last incremental provision
   * @return
   */
  public String getLastIncrementalMillisSince1970String() {
    return lastIncrementalMillisSince1970String;
  }

  /**
   * millis since last incremental provision
   * @param lastIncrementalMillisSince1970String
   */
  public void setLastIncrementalMillisSince1970String(String lastIncrementalMillisSince1970String) {
    this.lastIncrementalMillisSince1970String = lastIncrementalMillisSince1970String;
  }

  /**
   * last full summary
   * @return
   */
  public String getLastFullSummary() {
    return lastFullSummary;
  }

  /**
   * last full summary
   * @param lastFullSummary
   */
  public void setLastFullSummary(String lastFullSummary) {
    this.lastFullSummary = lastFullSummary;
  }

  /**
   * last incremental summary
   * @return
   */
  public String getLastIncrementalSummary() {
    return lastIncrementalSummary;
  }

  /**
   * last incremental summary
   * @param lastIncrementalSummary
   */
  public void setLastIncrementalSummary(String lastIncrementalSummary) {
    this.lastIncrementalSummary = lastIncrementalSummary;
  }
  
  
  /**
   * get the stem scope if assigned to a stem
   * @return the scope
   */
  public Scope getStemScope() {
    if (StringUtils.isBlank(this.stemScopeString)) {
      return Scope.SUB;
    }
    return Scope.valueOfIgnoreCase(this.stemScopeString, true);
  }
  
  
  /**
   * if stem scope sub
   * @return if sub
   */
  public boolean isStemScopeSub() {
    return Scope.SUB == this.getStemScope();
  }
  
  
  /**
   * copy a given attribute value object
   * @param from
   * @return
   */
  public static GrouperProvisioningAttributeValue copy(GrouperProvisioningAttributeValue from) {
    GrouperProvisioningAttributeValue value = new GrouperProvisioningAttributeValue();
    value.setDirectAssignment(from.isDirectAssignment());
    value.setDoProvision(from.getDoProvision());
    value.setLastFullMillisSince1970String(from.getLastFullMillisSince1970String());
    value.setLastFullSummary(from.getLastFullSummary());
    value.setLastIncrementalMillisSince1970String(from.getLastIncrementalMillisSince1970String());
    value.setLastIncrementalSummary(from.getLastIncrementalSummary());
    value.setOwnerStemId(from.getOwnerStemId());
    value.setStemScopeString(from.getStemScopeString());
    value.setTargetName(from.getTargetName());
    //value.setGrouperProvisioningObjectMetadata(from.getGrouperProvisioningObjectMetadata());
    value.setMetadataNameValues(from.getMetadataNameValues());
    return value;
  }

  
  public Map<String, Object> getMetadataNameValues() {
    return metadataNameValues;
  }

  
  public void setMetadataNameValues(Map<String, Object> metadataNameValues) {
    this.metadataNameValues = metadataNameValues;
  }

  
//  /**
//   * provisioning object metadata
//   * @return
//   */
//  public GrouperProvisioningObjectMetadata getGrouperProvisioningObjectMetadata() {
//    return grouperProvisioningObjectMetadata;
//  }
//
//  /**
//   * @param grouperProvisioningObjectMetadata
//   */
//  public void setGrouperProvisioningObjectMetadata(GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata) {
//    this.grouperProvisioningObjectMetadata = grouperProvisioningObjectMetadata;
//  }
  
  
  
  
}

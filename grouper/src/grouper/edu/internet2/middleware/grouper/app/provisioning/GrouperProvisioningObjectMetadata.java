package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningObjectMetadata {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningObjectMetadata.class);

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * list of metadata items for this metadata object
   */
  private List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = new ArrayList<GrouperProvisioningObjectMetadataItem>();

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }
  
  /**
   * list of metadata items for this metadata object
   * @return
   */
  public List<GrouperProvisioningObjectMetadataItem> getGrouperProvisioningObjectMetadataItems() {
    return grouperProvisioningObjectMetadataItems;
  }

  /**
   * list of metadata items for this metadata object
   * @param grouperProvisioningObjectMetadataItems
   */
  public void setGrouperProvisioningObjectMetadataItems(List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems) {
    this.grouperProvisioningObjectMetadataItems = grouperProvisioningObjectMetadataItems;
  }
  
  
  /**
   * build grouper provisioning object metadata object from json string
   * @param workflowApprovalStates
   * @return
   */
  public static GrouperProvisioningObjectMetadata buildGrouperProvisioningObjectMetadataFromJsonString(
      String provisioningObjectMetadata) {
    try {
      GrouperProvisioningObjectMetadata grouperProvisioningObjectMetadata = GrouperProvisioningSettings.objectMapper
          .readValue(provisioningObjectMetadata, GrouperProvisioningObjectMetadata.class);
      return grouperProvisioningObjectMetadata;
    } catch (Exception e) {
      LOG.error("could not convert: " + provisioningObjectMetadata
          + " to GrouperProvisioningObjectMetadata object");
      throw new RuntimeException(
          "could not convert json string to GrouperProvisioningObjectMetadata object", e);
    }

  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return
   */
  public Map<String, String> validateMetadataInputForFolder(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return
   */
  public Map<String, String> validateMetadataInputForGroup(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return
   */
  public Map<String, String> validateMetadataInputForMember(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  /**
   * return an error message if the value is wrong
   * @param name
   * @param value
   * @return
   */
  public Map<String, String> validateMetadataInputForMembership(Map<String, Object> nameToValueFromUsersInput) {
    return null;
  }
  
  
}

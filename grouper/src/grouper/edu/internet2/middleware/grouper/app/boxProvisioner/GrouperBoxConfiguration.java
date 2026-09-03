package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class GrouperBoxConfiguration extends GrouperProvisioningConfiguration {

  private String boxExternalSystemConfigId;
  
  private Set<String> entityAttributesToRetrieve = new LinkedHashSet<String>();
  private Set<String> groupAttributesToRetrieve = new LinkedHashSet<String>();
  
  @Override
  public void configureSpecificSettings() {
    
    this.boxExternalSystemConfigId = this.retrieveConfigString("boxExternalSystemConfigId", true);
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.getTargetGroupAttributeNameToConfig();
    
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values()) {
      
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.entityAttributesToRetrieve.add(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values()) {

      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.groupAttributesToRetrieve.add(grouperProvisioningConfigurationAttribute.getName());
      }
    }

    // When the generic sync-back mirror is capturing groups, ask box for EVERY group field, not
    // just the ones configured as target attributes.  Without this the mirror stores nulls for
    // anything the provisioner does not otherwise manage: the capture reads whatever json box
    // returned, and this set is what decides the box fields= parameter.  Note the two sides speak
    // different names -- this set holds GROUPER side names, which retrieveBoxGroups translates
    // through GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames (and silently skips
    // anything not in that map), while the native attribute configs are box json names.
    //
    // Cheap for groups and not for entities, which is why this is groups only: box returns all
    // groups in a single call regardless of field count, and there are rarely more than a few
    // hundred of them, so extra fields widen one response and add a few rows per group to the
    // mirror.  The entity side is tens of thousands of users, where each extra attribute is
    // another row per user on every sync.
    if (GrouperUtil.booleanValue(this.retrieveConfigBoolean("loadGroupsToGenericGrouperTable", false), false)) {

      for (String grouperAttributeName : GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet()) {

        // canInviteAsCollaborator maps to permissions.can_invite_as_collaborator, a nested json
        // path rather than a top level box field, so it cannot go in the fields= list
        if (StringUtils.equals("canInviteAsCollaborator", grouperAttributeName)) {
          continue;
        }
        this.groupAttributesToRetrieve.add(grouperAttributeName);
      }
    }

  }
  
  public String getBoxExternalSystemConfigId() {
    return boxExternalSystemConfigId;
  }
  
  public void setBoxExternalSystemConfigId(String boxExternalSystemConfigId) {
    this.boxExternalSystemConfigId = boxExternalSystemConfigId;
  }

  public Set<String> getEntityAttributesToRetrieve() {
    return entityAttributesToRetrieve;
  }

  public Set<String> getGroupAttributesToRetrieve() {
    return groupAttributesToRetrieve;
  }
  
}

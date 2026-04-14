package edu.internet2.middleware.grouper.app.datadog;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Translator for Datadog provisioner. When datadogAddTeamAdminMetadata is enabled,
 * automatically populates the "admins" group attribute from the md_adminGroupName metadata.
 * The metadata value should be the Grouper group path of an admin group.
 * Members of the admin group must also be members of the provisionable team group
 * to receive the admin role. Consider adding the admin group as a member of the
 * team group so all admins are automatically team members.
 */
public class DatadogProvisioningTranslator extends GrouperProvisioningTranslator {

  @Override
  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups,
      boolean includeDelete, boolean forCreate) {

    List<ProvisioningGroup> grouperTargetGroups = super.translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, forCreate);

    DatadogProvisionerConfiguration datadogConfiguration = (DatadogProvisionerConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    if (!datadogConfiguration.isDatadogAddTeamAdminMetadata()) {
      return grouperTargetGroups;
    }

    String adminGroupMetadataName = "md_adminGroupName";

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata()
        .getGrouperProvisioningObjectMetadataItemsByName().containsKey(adminGroupMetadataName)) {
      return grouperTargetGroups;
    }

    // skip if there is already a translation configured for admins
    GrouperProvisioningConfigurationAttribute configurationAttribute = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get("admins");
    if (configurationAttribute != null && configurationAttribute.getTranslateExpressionType() != null) {
      return grouperTargetGroups;
    }

    for (ProvisioningGroup grouperProvisioningGroup : GrouperUtil.nonNull(grouperProvisioningGroups)) {

      String adminGroupName = grouperProvisioningGroup.retrieveAttributeValueString(adminGroupMetadataName);
      if (StringUtils.isBlank(adminGroupName)) {
        continue;
      }

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        continue;
      }

      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup == null) {
        continue;
      }

      Set<String> adminEntityIds = provisioningGroupWrapper.groupMembers(adminGroupName, "entityAttributeValueCache0");
      grouperTargetGroup.assignAttributeValue("admins", adminEntityIds);
    }

    return grouperTargetGroups;
  }

}

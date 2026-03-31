package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Translator for TrueFoundry provisioner. When trueFoundryAddTeamManagerMetadata is enabled,
 * automatically populates the "managers" group attribute from the md_trueFoundryManagerGroupName metadata.
 * The metadata value should be the Grouper group path of the managers group.
 * Members of the managers group must also be members of the provisionable team group
 * to receive the manager role. Consider adding the managers group as a member of the
 * team group so all managers are automatically team members.
 */
public class TrueFoundryProvisioningTranslator extends GrouperProvisioningTranslator {

  @Override
  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups,
      boolean includeDelete, boolean forCreate) {

    List<ProvisioningGroup> grouperTargetGroups = super.translateGrouperToTargetGroups(grouperProvisioningGroups, includeDelete, forCreate);

    TrueFoundryProvisionerConfiguration config = (TrueFoundryProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    if (!config.isTrueFoundryAddTeamManagerMetadata()) {
      return grouperTargetGroups;
    }

    String managerGroupMetadataName = config.getTrueFoundryManagerGroupMetadataName();

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata()
        .getGrouperProvisioningObjectMetadataItemsByName().containsKey(managerGroupMetadataName)) {
      return grouperTargetGroups;
    }

    // skip if there is already a translation configured for managers
    GrouperProvisioningConfigurationAttribute configurationAttribute = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get("managers");
    if (configurationAttribute != null && configurationAttribute.getTranslateExpressionType() != null) {
      return grouperTargetGroups;
    }

    for (ProvisioningGroup grouperProvisioningGroup : GrouperUtil.nonNull(grouperProvisioningGroups)) {

      String managerGroupName = grouperProvisioningGroup.retrieveAttributeValueString(managerGroupMetadataName);
      if (StringUtils.isBlank(managerGroupName)) {
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

      Set<String> managerEntityIds = provisioningGroupWrapper.groupMembers(managerGroupName, "entityAttributeValueCache0");
      grouperTargetGroup.assignAttributeValue("managers", managerEntityIds);
    }

    return grouperTargetGroups;
  }

}

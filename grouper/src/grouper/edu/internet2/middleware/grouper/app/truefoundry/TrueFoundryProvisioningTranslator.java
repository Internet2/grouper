package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
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

      // On the first provision the entity attribute cache may not be populated yet
      // (it gets written after translation). Fall back to resolving entity IDs by
      // matching manager group memberIds to the provisioner's in-memory entity wrappers
      // and computing the ID from the grouperProvisioningEntity email field.
      if (GrouperUtil.length(managerEntityIds) == 0) {
        Set<String> managerMemberIds = provisioningGroupWrapper.groupMembers(managerGroupName, "memberId");
        if (GrouperUtil.length(managerMemberIds) > 0) {
          Map<String, ProvisioningEntityWrapper> memberIdToEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();
          for (ProvisioningEntityWrapper entityWrapper : GrouperUtil.nonNull(
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
            if (entityWrapper.getMemberId() != null) {
              memberIdToEntityWrapper.put(entityWrapper.getMemberId(), entityWrapper);
            }
          }
          managerEntityIds = new HashSet<String>();
          for (String managerMemberId : managerMemberIds) {
            ProvisioningEntityWrapper entityWrapper = memberIdToEntityWrapper.get(managerMemberId);
            if (entityWrapper != null) {
              // Use the same field that entityAttributeValueCache0 is configured from:
              // the entity "id" which in TrueFoundry is derived from the email field.
              Object entityId = this.translateFromGrouperProvisioningEntityField(
                  entityWrapper, "email");
              if (entityId != null && !StringUtils.isBlank(entityId.toString())) {
                managerEntityIds.add(entityId.toString());
              }
            }
          }
        }
      }

      grouperTargetGroup.assignAttributeValue("managers", managerEntityIds);
    }

    return grouperTargetGroups;
  }

}

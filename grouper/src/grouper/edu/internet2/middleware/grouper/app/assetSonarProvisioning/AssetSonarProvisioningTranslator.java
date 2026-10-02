package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Translator for the AssetSonar provisioner. After the configured attribute translations run, it
 * stamps two computed attributes on each Grouper-side target entity:
 * <ul>
 *   <li><b>role_id</b> (if configured as a target entity attribute): the access tier. A member of
 *       the configured administrator group gets administratorRoleId, else a member of the agent
 *       group gets agentRoleId, else staffUserRoleId (the non-login custodian tier). Highest tier
 *       wins. Role ids come from the external system because they are tenant-specific.</li>
 *   <li><b>status</b> (if configured): always 1. Every entity Grouper translates is provisionable,
 *       so it must be active; a deactivated member that comes back into scope is reactivated by
 *       the ordinary update diff. Removal (status 0) happens in the DAO's deleteEntity.</li>
 * </ul>
 *
 * <p>The tier groups are read directly from Grouper, like Dropbox's admin-role marker groups, so
 * they do not need to be marked provisionable themselves.</p>
 *
 * <p>A tier whose group is not configured is <i>unmanaged</i>: Grouper never assigns it, and
 * {@link AssetSonarTargetDao#updateEntity} never moves a member out of it. That keeps an
 * Administrator who was set up by hand from being demoted to Staff User by the first full sync.</p>
 */
public class AssetSonarProvisioningTranslator extends GrouperProvisioningTranslator {

  @Override
  public List<ProvisioningEntity> translateGrouperToTargetEntities(List<ProvisioningEntity> grouperProvisioningEntities,
      boolean includeDelete, boolean forCreate) {

    List<ProvisioningEntity> grouperTargetEntities = super.translateGrouperToTargetEntities(
        grouperProvisioningEntities, includeDelete, forCreate);

    AssetSonarProvisionerConfiguration config = (AssetSonarProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    Map<String, GrouperProvisioningConfigurationAttribute> targetEntityAttributes =
        GrouperUtil.nonNull(config.getTargetEntityAttributeNameToConfig());
    boolean manageRoleId = targetEntityAttributes.containsKey(AssetSonarMember.ATTR_ROLE_ID);
    boolean manageStatus = targetEntityAttributes.containsKey(AssetSonarMember.ATTR_STATUS);

    if (!manageRoleId && !manageStatus) {
      return grouperTargetEntities;
    }

    String externalSystemConfigId = config.getAssetSonarExternalSystemConfigId();
    String staffUserRoleId = null;
    String agentRoleId = null;
    String administratorRoleId = null;
    Set<String> administratorMemberIds = new HashSet<String>();
    Set<String> agentMemberIds = new HashSet<String>();

    if (manageRoleId) {
      staffUserRoleId = AssetSonarExternalSystem.retrieveConfigValue(externalSystemConfigId, "staffUserRoleId", true);

      if (!StringUtils.isBlank(config.getAssetSonarAdministratorGroupName())) {
        administratorRoleId = AssetSonarExternalSystem.retrieveConfigValue(externalSystemConfigId, "administratorRoleId", true);
        administratorMemberIds = memberIdsOfGroup(config.getAssetSonarAdministratorGroupName());
      }
      if (!StringUtils.isBlank(config.getAssetSonarAgentGroupName())) {
        agentRoleId = AssetSonarExternalSystem.retrieveConfigValue(externalSystemConfigId, "agentRoleId", true);
        agentMemberIds = memberIdsOfGroup(config.getAssetSonarAgentGroupName());
      }
    }

    for (ProvisioningEntityWrapper entityWrapper : GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      ProvisioningEntity targetEntity = entityWrapper.getGrouperTargetEntity();
      String memberId = entityWrapper.getMemberId();
      if (memberId == null || targetEntity == null) {
        continue;
      }
      if (manageRoleId) {
        // highest tier wins
        String roleId = staffUserRoleId;
        if (administratorMemberIds.contains(memberId)) {
          roleId = administratorRoleId;
        } else if (agentMemberIds.contains(memberId)) {
          roleId = agentRoleId;
        }
        targetEntity.assignAttributeValue(AssetSonarMember.ATTR_ROLE_ID, roleId);
      }
      if (manageStatus) {
        targetEntity.assignAttributeValue(AssetSonarMember.ATTR_STATUS, AssetSonarMember.STATUS_ACTIVE);
      }
    }

    return grouperTargetEntities;
  }

  /**
   * Read the member ids of a tier group directly from Grouper. A missing group is an error: a
   * typo in the config would otherwise silently demote everyone in that tier.
   * @param groupName full group name
   * @return Grouper member ids (effective members)
   */
  private Set<String> memberIdsOfGroup(String groupName) {

    Set<String> memberIds = new HashSet<String>();

    // a provisioning daemon runs inside a Grouper session; fall back to a root session if absent
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    if (grouperSession == null) {
      try {
        grouperSession = GrouperSession.startRootSession();
      } catch (SessionException se) {
        throw new RuntimeException("Could not start a Grouper session to resolve AssetSonar tier groups", se);
      }
      startedSession = true;
    }

    try {
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group == null) {
        throw new RuntimeException("AssetSonar tier group does not exist: '" + groupName + "'");
      }
      for (Member member : GrouperUtil.nonNull(group.getMembers())) {
        memberIds.add(member.getUuid());
      }
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

    return memberIds;
  }

}

package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Translator for the Smartsheet provisioner. After the configured attribute translations run, it
 * stamps the group-driven attributes on each Grouper-side target entity:
 * <ul>
 *   <li><b>seatType</b> (when seat types are managed): MEMBER if the user is in the member seat group,
 *       else GUEST if in the guest group, else CONTRIBUTOR if in the contributor group (highest seat
 *       wins), else smartsheetSeatTypeDefault. If none applies the attribute is removed, so the
 *       framework does not compare it and the user's seat is left alone.</li>
 *   <li><b>admin, groupAdmin, licensedSheetCreator, resourceViewer</b> (each only when its group is
 *       configured): "true" for members of the group, "false" for everyone else provisioned.</li>
 * </ul>
 *
 * <p>Membership is checked with {@link ProvisioningEntityWrapper#isInGroup(String)}, which records a
 * group-user dependency, so the groups do not need to be provisionable and a membership change in
 * one of them triggers an incremental recalc of that user. The dependency is recorded on the first
 * translation, so after configuring or changing a group name run a full sync.</p>
 */
public class SmartsheetProvisioningTranslator extends GrouperProvisioningTranslator {

  /** group names verified to exist this run, so the check happens once per group */
  private Set<String> groupNamesVerified = new HashSet<String>();

  @Override
  public List<ProvisioningEntity> translateGrouperToTargetEntities(List<ProvisioningEntity> grouperProvisioningEntities,
      boolean includeDelete, boolean forCreate) {

    List<ProvisioningEntity> grouperTargetEntities = super.translateGrouperToTargetEntities(
        grouperProvisioningEntities, includeDelete, forCreate);

    SmartsheetProvisionerConfiguration config = (SmartsheetProvisionerConfiguration)
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    boolean manageSeatType = config.isManageSeatType();
    Map<String, String> flagAttributeToGroupName = config.getFlagAttributeToGroupName();
    if (!manageSeatType && flagAttributeToGroupName.isEmpty()) {
      return grouperTargetEntities;
    }

    // a typo in a group name would otherwise quietly unassign that seat / flag for everyone
    verifyGroupExists(config.getSmartsheetSeatTypeMemberGroupName());
    verifyGroupExists(config.getSmartsheetSeatTypeGuestGroupName());
    verifyGroupExists(config.getSmartsheetSeatTypeContributorGroupName());
    for (String groupName : flagAttributeToGroupName.values()) {
      verifyGroupExists(groupName);
    }

    for (ProvisioningEntity grouperProvisioningEntity : GrouperUtil.nonNull(grouperProvisioningEntities)) {

      ProvisioningEntityWrapper entityWrapper = grouperProvisioningEntity.getProvisioningEntityWrapper();
      if (entityWrapper == null || StringUtils.isBlank(entityWrapper.getMemberId())) {
        continue;
      }
      ProvisioningEntity grouperTargetEntity = entityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity == null) {
        continue;
      }

      for (String flagAttribute : flagAttributeToGroupName.keySet()) {
        boolean hasFlag = entityWrapper.isInGroup(flagAttributeToGroupName.get(flagAttribute));
        grouperTargetEntity.assignAttributeValue(flagAttribute, Boolean.toString(hasFlag));
      }

      if (manageSeatType) {
        String seatType = computeSeatType(config, entityWrapper);
        if (seatType == null) {
          // unmanaged for this user: not compared, so the seat is left alone
          grouperTargetEntity.removeAttribute(SmartsheetUser.ATTR_SEAT_TYPE);
        } else {
          grouperTargetEntity.assignAttributeValue(SmartsheetUser.ATTR_SEAT_TYPE, seatType);
        }
      }
    }

    return grouperTargetEntities;
  }

  /**
   * Highest seat wins: MEMBER, then GUEST, then CONTRIBUTOR, then the default seat.
   * @param config the provisioner config
   * @param entityWrapper the entity
   * @return the seat type, or null if Grouper does not manage this user's seat
   */
  static String computeSeatType(SmartsheetProvisionerConfiguration config, ProvisioningEntityWrapper entityWrapper) {
    if (isInGroupIfConfigured(entityWrapper, config.getSmartsheetSeatTypeMemberGroupName())) {
      return SmartsheetUser.SEAT_MEMBER;
    }
    if (isInGroupIfConfigured(entityWrapper, config.getSmartsheetSeatTypeGuestGroupName())) {
      return SmartsheetUser.SEAT_GUEST;
    }
    if (isInGroupIfConfigured(entityWrapper, config.getSmartsheetSeatTypeContributorGroupName())) {
      return SmartsheetUser.SEAT_CONTRIBUTOR;
    }
    return config.getSmartsheetSeatTypeDefault();
  }

  private static boolean isInGroupIfConfigured(ProvisioningEntityWrapper entityWrapper, String groupName) {
    return !StringUtils.isBlank(groupName) && entityWrapper.isInGroup(groupName);
  }

  /**
   * Fail the sync if a configured group does not exist (checked once per group per run).
   * @param groupName the group name, may be blank
   */
  private void verifyGroupExists(String groupName) {
    if (StringUtils.isBlank(groupName) || this.groupNamesVerified.contains(groupName)) {
      return;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    boolean startedSession = false;
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession();
      startedSession = true;
    }
    try {
      Group group = GroupFinder.findByName(grouperSession, groupName, false);
      if (group == null) {
        throw new RuntimeException("Smartsheet provisioner '" + this.getGrouperProvisioner().getConfigId()
            + "': configured group does not exist: '" + groupName + "'");
      }
      this.groupNamesVerified.add(groupName);
    } finally {
      if (startedSession) {
        GrouperSession.stopQuietly(grouperSession);
      }
    }
  }

}

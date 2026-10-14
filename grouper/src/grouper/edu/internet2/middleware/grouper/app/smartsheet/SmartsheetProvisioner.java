package edu.internet2.middleware.grouper.app.smartsheet;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Provisioner for Smartsheet organization users, groups, and group memberships (Smartsheet API 2.0).
 *
 * <p>Design: Smartsheet groups are org-level lists of users that sheets, workspaces, and reports
 * are shared with. That maps onto Grouper's membershipObjects model:</p>
 * <ul>
 *   <li>target group      = a Smartsheet group (name unique in the org), created / renamed /
 *                           deleted by Grouper</li>
 *   <li>target entity     = a Smartsheet org user, matched by email, added to the org by Grouper,
 *                           and deactivated (default) or removed when no longer provisioned</li>
 *   <li>target membership = the user's presence in the group's members list</li>
 * </ul>
 *
 * <p>The API token in the external system should belong to a System Admin (user writes and
 * the user status / account flags need it).</p>
 */
public class SmartsheetProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return SmartsheetTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return SmartsheetProvisionerConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    // computes the group-driven seatType and account flags
    return SmartsheetProvisioningTranslator.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningCompare> grouperProvisioningCompareClass() {
    // seatType compare: VIEWER equals CONTRIBUTOR, provisional members are not downgraded by default
    return SmartsheetProvisioningCompare.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTargetNativeSync> grouperProvisioningTargetNativeSyncClass() {
    // sync-back: native user / group / membership capture
    return SmartsheetProvisioningTargetNativeSync.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // a group member is a first-class object (an entry in the group's members list), so this is
    // membershipObjects provisioning, not membership-as-group-attribute
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(
        GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}

package edu.internet2.middleware.grouper.app.jamf;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Provisioner for Jamf Pro admin accounts and account groups (roles).
 *
 * <p>Design: Jamf models admin access as "account groups" (roles) that hold a
 * privilege set, and individual "accounts" that become members of one or more
 * roles. That maps directly onto Grouper's membershipObjects model:</p>
 * <ul>
 *   <li>target group   = a Jamf account group / role (READ ONLY -- Jamf admins own the
 *                        privilege definitions; Grouper never creates/updates/deletes roles)</li>
 *   <li>target entity  = a Jamf admin account, matched/created by name = lowercased EPPN
 *                        (pennkey@upenn.edu). Grouper creates accounts it needs but never
 *                        updates or deletes them.</li>
 *   <li>target membership = the account's presence in a role's &lt;members&gt; list, which
 *                        Grouper fully owns (add/remove).</li>
 * </ul>
 *
 * <p>The Jamf Classic API has no atomic single-member add/remove for account groups, so
 * membership changes are applied by retrieve-modify-write of the whole &lt;members&gt; list
 * (see {@link JamfTargetDao}).</p>
 */
public class JamfProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return JamfTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return JamfProvisionerConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTargetNativeSync> grouperProvisioningTargetNativeSyncClass() {
    return JamfProvisioningTargetNativeSync.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // an account's role membership is a first-class object (the <members> entry), so this is
    // membershipObjects provisioning, not membership-as-group-attribute.
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(
        GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}

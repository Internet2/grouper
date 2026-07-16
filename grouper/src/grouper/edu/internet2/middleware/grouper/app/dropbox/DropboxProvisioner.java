package edu.internet2.middleware.grouper.app.dropbox;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Grouper provisioner for Dropbox Business.  Provisions team groups, team members
 * (entities), and group memberships against the Dropbox Team API.  Optionally
 * manages per-member admin roles sourced from a configured Grouper folder.
 */
public class DropboxProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return DropboxTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return DropboxProvisionerConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return DropboxProvisioningTranslator.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // Dropbox group members are first-class associations (groups/members/add|remove),
    // so memberships are modeled as objects
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(
        GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}

package edu.internet2.middleware.grouper.app.libreChat;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Provisioner for LibreChat groups, so people can share agents, prompts and MCP servers with a group
 * whose membership Grouper keeps current.
 *
 * <ul>
 *   <li>target group = a LibreChat group (source local) that Grouper creates, renames and deletes.
 *       Grouper only touches groups whose idOnTheSource starts with the configured prefix.</li>
 *   <li>target entity = a LibreChat user, matched by username (e.g. eppn).  Read only: LibreChat
 *       creates users at first login, so a subject without a user is skipped until they log in.</li>
 *   <li>target membership = the user in the group's members, added and removed one at a time.</li>
 * </ul>
 *
 * GRP-7594
 */
public class LibreChatProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return LibreChatTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return LibreChatProvisionerConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // each member is added and removed with its own call, so memberships are objects
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(
        GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}

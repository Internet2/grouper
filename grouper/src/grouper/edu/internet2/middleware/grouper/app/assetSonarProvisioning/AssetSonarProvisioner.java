package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Provisioner for AssetSonar (EZO IT asset inventory) members.
 *
 * <p><b>Entities only.</b> AssetSonar has no group construct that governs access: the access tier
 * is a field on the member ({@code role_id}), and Teams describe asset ownership (and have no
 * membership API). So there are no target groups and no memberships. The tier is computed from
 * Grouper group membership by {@link AssetSonarProvisioningTranslator}.</p>
 *
 * <p>Members are matched on email (the only attribute AssetSonar enforces uniqueness on), the
 * numeric member id is cached in the sync tables, and removal is a deactivation
 * ({@code status=0}), never a DELETE, because members carry asset checkout history.</p>
 */
public class AssetSonarProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return AssetSonarTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return AssetSonarProvisionerConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return AssetSonarProvisioningTranslator.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // entity-only, configured like the Interfolio provisioner: membershipObjects with
    // operateOnGrouperGroups=false and operateOnGrouperMemberships=false, so subjects in provisionable
    // groups become target entities and nothing else is written. entityAttributes would need an
    // entityMembershipAttributeName, and the tier is computed by the translator instead.
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(
        GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

}

package edu.internet2.middleware.grouper.app.duo.role;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeManipulation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Duo admin role provisioner: provisions Duo administrators and their admin role (role_id) based on
 * which role groups they are in
 */
public class GrouperDuoRoleProvisioner extends GrouperProvisioner {
  
  
  public GrouperDuoRoleProvisioner() {
    super();
    this.retrieveGrouperProvisioningBehavior().setCreateGroupsAndEntitiesBeforeTranslatingMemberships(false);
  }

  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return GrouperDuoRoleTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GrouperDuoRoleConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.entityAttributes);
  }
  
  @Override
  protected Class<? extends GrouperProvisioningAttributeManipulation> grouperProvisioningAttributeManipulationClass() {
    return DuoRoleProvisioningAttributeManipulation.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return DuoRoleProvisoningConfigurationValidation.class;
  }
  
  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return DuoRoleSyncObjectMetadata.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return GrouperDuoRoleTranslator.class;
  }

  /**
   * resolves role names and ids to Duo role_id values, and caches custom roles for this run
   */
  private GrouperDuoRoleResolver grouperDuoRoleResolver = null;

  /**
   * @return the role resolver for this provisioner run
   */
  public GrouperDuoRoleResolver retrieveDuoRoleResolver() {
    if (this.grouperDuoRoleResolver == null) {
      GrouperDuoRoleConfiguration duoRoleConfiguration = (GrouperDuoRoleConfiguration)this.retrieveGrouperProvisioningConfiguration();
      this.grouperDuoRoleResolver = new GrouperDuoRoleResolver(duoRoleConfiguration.getDuoExternalSystemConfigId(), 
          duoRoleConfiguration.getRolePriority());
    }
    return this.grouperDuoRoleResolver;
  }

}

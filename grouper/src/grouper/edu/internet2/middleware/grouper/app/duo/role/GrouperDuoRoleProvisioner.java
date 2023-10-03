package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeManipulation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.util.GrouperUtil; 

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

  public static String pickHighestPriorityRoleName(Collection<String> roleNames) {
    roleNames = GrouperUtil.nonNull(roleNames);
    if (roleNames.contains("Owner")) {
      return "Owner";
    } else if (roleNames.contains("Administrator")) {
      return "Administrator";
    } else if (roleNames.contains("Application Manager")) {
      return "Application Manager";
    } else if (roleNames.contains("User Manager")) {
      return "User Manager";
    } else if (roleNames.contains("Help Desk")) {
      return "Help Desk";
    } else if (roleNames.contains("Billing")) {
      return "Billing";
//    } else if (roleNames.contains("Phishing Manager")) {
//      return "Phishing Manager";
    } else if (roleNames.contains("Read-only")) {
      return "Read-only";
    }
    
    throw new RuntimeException("Invalid role names: " + GrouperUtil.toStringForLog(roleNames));
  }
  
  
  

}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningType;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * sync to ldap
 */
public class LdapSync extends GrouperProvisioner {

  /**
   * log object
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(LdapSync.class);

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> retrieveGrouperTargetDaoClass() {
    return LdapProvisioningTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> retrieveGrouperProvisioningConfigurationClass() {
    return LdapSyncConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslatorBase> retrieveGrouperTranslatorClass() {
    return LdapProvisioningTranslator.class;
  }

  public LdapSyncConfiguration retrieveLdapProvisioningConfiguration() {
    return (LdapSyncConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }

  @Override
  public void registerProvisioningBehaviors(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {
    
    LdapSyncConfiguration ldapSyncConfiguration = this.retrieveLdapProvisioningConfiguration();
    switch (ldapSyncConfiguration.getLdapProvisioningType()) {
      case groupMemberships:
        
        grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.groupAttributes);
        
        // TODO some or all of this needs to be inferred based on config and perhaps put into GrouperProvisioningBehavior instead
        System.out.println("temp code in LdapSync regarding behaviors needs to be fixed");
        grouperProvisioningBehavior.setGroupsRetrieveAll(true);
        grouperProvisioningBehavior.setGroupsUpdate(true);
        grouperProvisioningBehavior.setGroupsDeleteIfNotInGrouper(true);
        grouperProvisioningBehavior.setGroupsDeleteIfDeletedFromGrouper(true);

       break;
        
      case userAttributes:
        
        grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.entityAttributes);
        break;
        
      default:
          throw new RuntimeException("Not expecting type: " + ldapSyncConfiguration.getLdapProvisioningType());
    }
  }

}

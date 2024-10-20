package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    if (!this.retrieveGrouperProvisioningConfiguration().isConfigured()) {
      throw new RuntimeException("Why is provisioner not configured???");
    }
    // TODO fix this
//    return SqlProvisioningType.sqlLikeLdapGroupMemberships.sqlTargetDaoClass();
    
    return SqlProvisioningDao.class;
  }

  public SqlProvisioningConfiguration retrieveSqlProvisioningConfiguration() {
    return (SqlProvisioningConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }
  
  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return SqlProvisioningConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {

  // TODO
  //  this.retrieveSqlProvisioningConfiguration().getSqlProvisioningType().registerProvisioningBehaviors(
  //      grouperProvisioningBehavior);
    
  }

}

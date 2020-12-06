package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
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
    return this.retrieveSqlProvisioningConfiguration().getSqlProvisioningType().sqlTargetDaoClass();
  }

  public SqlProvisioningConfiguration retrieveSqlProvisioningConfiguration() {
    return (SqlProvisioningConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }
  
  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return SqlProvisioningConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(
      GrouperProvisioningBehavior grouperProvisioningBehavior) {

    this.retrieveSqlProvisioningConfiguration().getSqlProvisioningType().registerProvisioningBehaviors(
        grouperProvisioningBehavior);
    
  }

}

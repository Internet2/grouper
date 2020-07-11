package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlMembershipProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> retrieveTargetDaoClass() {
    return SqlProvisioningDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> retrieveProvisioningConfigurationClass() {
    // TODO Auto-generated method stub
    return SqlProvisioningConfiguration.class;
  }

}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogicAlgorithmBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogicAlgorithmGroupsOnly;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
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
  protected Class<? extends GrouperProvisionerTargetDaoBase> retrieveTargetDaoClass() {
    return LdapProvisioningTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> retrieveProvisioningConfigurationClass() {
    return LdapSyncConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslatorBase> retrieveTranslatorClass() {
    return LdapProvisioningTranslator.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningLogicAlgorithmBase> retrieveProvisioningLogicAlgorithmClass() {
    return GrouperProvisioningLogicAlgorithmGroupsOnly.class;
  }
  
  
}

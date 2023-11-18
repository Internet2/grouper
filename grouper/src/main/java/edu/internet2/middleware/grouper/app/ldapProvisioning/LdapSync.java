/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;


/**
 * sync to ldap
 */
public class LdapSync extends GrouperProvisioner {
  
  @Override
  protected Class<? extends GrouperProvisioningCompare> grouperProvisioningCompareClass() {
    return LdapSyncCompare.class;
  }

  /**
   * log object
   */
  @SuppressWarnings("unused")
  private static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(LdapSync.class);

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return LdapProvisioningTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return LdapSyncConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslator> grouperTranslatorClass() {
    return LdapProvisioningTranslator.class;
  }

  public LdapSyncConfiguration retrieveLdapProvisioningConfiguration() {
    return (LdapSyncConfiguration)this.retrieveGrouperProvisioningConfiguration();
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return LdapSyncConfigurationValidation.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningDiagnosticsContainer> grouperProvisioningDiagnosticsContainerClass() {
    return LdapSyncDiagnosticsContainer.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningObjectMetadata> grouperProvisioningObjectMetadataClass() {
    return LdapSyncObjectMetadata.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningLogic> grouperProvisioningLogicClass() {
    return LdapSyncLogic.class;
  } 
  
}

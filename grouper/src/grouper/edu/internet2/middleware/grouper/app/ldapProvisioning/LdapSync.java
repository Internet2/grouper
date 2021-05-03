/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogic;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadata;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslatorBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


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
  private static final Log LOG = LogFactory.getLog(LdapSync.class);

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return LdapProvisioningTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationBase> grouperProvisioningConfigurationClass() {
    return LdapSyncConfiguration.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningTranslatorBase> grouperTranslatorClass() {
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

package edu.internet2.middleware.grouper.app.genericProvisioner;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLoader;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperGenericProvisioner extends GrouperProvisioner {
  
  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    GrouperGenericConfiguration grouperGenericConfiguration = (GrouperGenericConfiguration)this.retrieveGrouperProvisioningConfiguration();
    String className = grouperGenericConfiguration.getGenericProvisionerDaoClassName();
    Class<? extends GrouperProvisionerTargetDaoBase> theClass = GrouperUtil.forName(className);
    return theClass;
  }

  @Override
  protected Class<? extends GrouperProvisioningLoader> grouperProvisioningLoaderClass() {
    GrouperGenericConfiguration grouperGenericConfiguration = (GrouperGenericConfiguration)this.retrieveGrouperProvisioningConfiguration();
    String className = grouperGenericConfiguration.getGenericLoaderClassName();
    if (StringUtils.isBlank(className)) {
      return null;
    }
    Class<? extends GrouperProvisioningLoader> theClass = GrouperUtil.forName(className);
    return theClass;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return GrouperGenericConfiguration.class;
  }

}

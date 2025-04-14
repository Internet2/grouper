package edu.internet2.middleware.grouper.app.gshTemplateProvisioner;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRunAsType;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFactory;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GshTemplateProvisionerFactory extends GrouperProvisioner implements GrouperProvisioningFactory {
  
  @Override
  public GrouperProvisioner generateGrouperProvisioner(String configId) {
    
    String gshTemplateConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "provisioner." + configId + ".gshTemplateConfigId");
    
    GshTemplateExec exec = new GshTemplateExec();

    exec.assignConfigId(gshTemplateConfigId);

    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(gshTemplateConfigId);
    gshTemplateConfig.populateConfiguration();
    gshTemplateConfig.setGshTemplateRunAsType(GshTemplateRunAsType.GrouperSystem);
    GrouperUtil.assertion(gshTemplateConfig.isEnabled(), "GshTemplate '" + gshTemplateConfigId + "' is not enabled!");

    exec.assignCurrentUser(SubjectFinder.findRootSubject());
    
    GshTemplateExecOutput gshTemplateExecOutput = exec.execute();

    if (gshTemplateExecOutput.getException() != null) {
      throw gshTemplateExecOutput.getException();
    }

    GrouperProvisioner grouperProvisioner = gshTemplateExecOutput.getGshTemplateOutput().retrieveGrouperProvisioner();

    GrouperUtil.assertion(grouperProvisioner != null, "gshTemplateOutput.retrieveGrouperProvisioner() is null for '" + gshTemplateConfigId + "'!");
    
    return grouperProvisioner;
    
  }

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    throw new RuntimeException("should not be called");
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    throw new RuntimeException("should not be called");
  }

}

package edu.internet2.middleware.grouper.app.interfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * UI registration for the Interfolio provisioner.  Makes it appear in the provisioner UI dropdown and
 * maps the UI config system to {@link InterfolioProvisioner}.
 */
public class InterfolioProvisionerConfiguration extends ProvisioningConfiguration {

  @Override
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    return new ArrayList<ProvisionerStartWithBase>();
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return InterfolioProvisioner.class.getName();
  }

}

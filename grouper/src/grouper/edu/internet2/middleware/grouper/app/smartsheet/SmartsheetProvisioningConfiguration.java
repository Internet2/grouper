package edu.internet2.middleware.grouper.app.smartsheet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * UI registration for the Smartsheet provisioner. This is what makes the provisioner appear in the
 * config UI dropdown and ties the "provisioner.&lt;id&gt;.class" property to
 * {@link SmartsheetProvisioner}.
 *
 * <p>Note: this is distinct from {@link SmartsheetProvisionerConfiguration}, which holds the
 * runtime config values. This class only wires the UI/config framework.</p>
 */
public class SmartsheetProvisioningConfiguration extends ProvisioningConfiguration {

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
    return SmartsheetProvisioner.class.getName();
  }
}

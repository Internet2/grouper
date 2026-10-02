package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * UI registration for the AssetSonar provisioner. This is what makes the provisioner appear in
 * the config UI dropdown and ties the "provisioner.&lt;id&gt;.class" property to
 * {@link AssetSonarProvisioner}.
 *
 * <p>Distinct from {@link AssetSonarProvisionerConfiguration}, which holds the runtime config
 * values.</p>
 */
public class AssetSonarProvisioningConfiguration extends ProvisioningConfiguration {

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
    return AssetSonarProvisioner.class.getName();
  }
}

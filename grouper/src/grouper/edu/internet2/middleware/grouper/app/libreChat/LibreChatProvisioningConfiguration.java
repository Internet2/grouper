package edu.internet2.middleware.grouper.app.libreChat;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * UI registration for the LibreChat provisioner: makes it appear in the provisioner config dropdown and
 * ties provisioner.&lt;id&gt;.class to {@link LibreChatProvisioner}.  Runtime settings are in
 * {@link LibreChatProvisionerConfiguration}.
 *
 * GRP-7594
 */
public class LibreChatProvisioningConfiguration extends ProvisioningConfiguration {

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
    return LibreChatProvisioner.class.getName();
  }

}

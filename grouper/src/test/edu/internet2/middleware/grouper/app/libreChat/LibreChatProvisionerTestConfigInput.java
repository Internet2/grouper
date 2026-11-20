package edu.internet2.middleware.grouper.app.libreChat;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder-pattern config input for LibreChat provisioner tests (GRP-7594)
 */
public class LibreChatProvisionerTestConfigInput {

  /** provisioner config id (required) */
  private String configId = null;

  /** extra config by suffix and value (overrides defaults) */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  public LibreChatProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  public String getConfigId() {
    return this.configId;
  }

  public LibreChatProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

}

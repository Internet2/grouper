package edu.internet2.middleware.grouper.app.interfolio;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder-style config input for the Interfolio provisioner tests.
 */
public class InterfolioProvisionerTestConfigInput {

  /** extra config by suffix and value */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /**
   * add extra config by suffix and value
   * @param suffix the property suffix
   * @param value the value
   * @return this for chaining
   */
  public InterfolioProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  /**
   * @return extra config by suffix and value
   */
  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  /** default to myInterfolioProvisioner */
  private String configId = "myInterfolioProvisioner";

  /**
   * @param string the config id
   * @return this for chaining
   */
  public InterfolioProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

}

package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.HashMap;
import java.util.Map;

public class BoxProvisionerTestConfigInput {
  
  /**
   * extra config by suffix and value
   */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /**
   * extra config by suffix and value
   * @param suffix
   * @param value
   * @return this for chaining
   */
  public BoxProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  
  /**
   * extra config by suffix and value
   * @return map
   */
  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  /**
   * default to myBoxProvisioner
   */
  private String configId = "myBoxProvisioner";

  /**
   * default to myBoxProvisioner
   * @param string
   * @return this for chaining
   */
  public BoxProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myBoxProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

}

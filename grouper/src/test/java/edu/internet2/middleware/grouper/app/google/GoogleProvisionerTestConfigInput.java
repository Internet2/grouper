package edu.internet2.middleware.grouper.app.google;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class GoogleProvisionerTestConfigInput {

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
  public GoogleProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myGoogleProvisioner
   */
  private String configId = "myGoogleProvisioner";

  /**
   * default to myGoogleProvisioner
   * @param string
   * @return this for chaining
   */
  public GoogleProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myGoogleProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
}

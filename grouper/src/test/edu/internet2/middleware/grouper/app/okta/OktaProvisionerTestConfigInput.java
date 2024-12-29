package edu.internet2.middleware.grouper.app.okta;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class OktaProvisionerTestConfigInput {

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
  public OktaProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myOktaProvisioner
   */
  private String configId = "myOktaProvisioner";

  /**
   * default to myOktaProvisioner
   * @param string
   * @return this for chaining
   */
  public OktaProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myOktaProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
}

package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.HashMap;
import java.util.Map;


public class RemedyProvisionerTestConfigInput {
  
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
  public RemedyProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myRemedyProvisioner
   */
  private String configId = "myRemedyProvisioner";

  /**
   * default to myRemedyProvisioner
   * @param string
   * @return this for chaining
   */
  public RemedyProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myRemedyProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

}

package edu.internet2.middleware.grouper.app.midPointProvisioning;

import java.util.HashMap;
import java.util.Map;

public class MidPointProvisionerTestConfigInput {
  
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
  public MidPointProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  
  /**
   * default to midPointProvTest
   */
  private String configId = "midPointProvTest";
  /**
   * default to sqlProvTest
   * @param string
   * @return this for chaining
   */
  public MidPointProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to sqlProvTest
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

  
  public Map<String, String> getExtraConfig() {
    return extraConfig;
  }
  

}

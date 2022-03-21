package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class SqlProvisionerTestConfigInput {

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
  public SqlProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to sqlProvTest
   */
  private String configId = "sqlProvTest";

  /**
   * default to sqlProvTest
   * @param string
   * @return this for chaining
   */
  public SqlProvisionerTestConfigInput assignConfigId(String string) {
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
  
}

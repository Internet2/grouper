package edu.internet2.middleware.grouper.app.duo.role;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class DuoRoleProvisionerTestConfigInput {

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
  public DuoRoleProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myDuoProvisioner
   */
  private String configId = "myDuoRoleProvisioner";

  /**
   * default to myDuoProvisioner
   * @param string
   * @return this for chaining
   */
  public DuoRoleProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myDuoProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
}

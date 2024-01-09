package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.HashMap;
import java.util.Map;

public class TeamDynamixProvisionerTestConfigInput {
  
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
  public TeamDynamixProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myTeamDynamixProvisioner
   */
  private String configId = "myTeamDynamixProvisioner";

  /**
   * default to myTeamDynamixProvisioner
   * @param string
   * @return this for chaining
   */
  public TeamDynamixProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myTeamDynamixProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

}

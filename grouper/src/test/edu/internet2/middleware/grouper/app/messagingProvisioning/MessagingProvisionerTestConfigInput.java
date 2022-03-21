package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class MessagingProvisionerTestConfigInput {

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
  public MessagingProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * default to myMessagingProvisioner
   */
  private String configId = "myMessagingProvisioner";

  /**
   * default to myMessagingProvisioner
   * @param string
   * @return this for chaining
   */
  public MessagingProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myMessagingProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
}

package edu.internet2.middleware.grouper.app.datadog;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;

/**
 * Config input for Datadog provisioner tests
 */
public class DatadogProvisionerTestConfigInput {

  /**
   * provisioner config id (required)
   */
  private String configId = null;

  /**
   * provisioner config id
   * @param string
   * @return this for chaining
   */
  public DatadogProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * provisioner config id
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }

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
  public DatadogProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * optional group to restrict which users get provisioned
   */
  private Group groupOfUsersToProvision;

  /**
   * group of users to provision
   * @return group
   */
  public Group getGroupOfUsersToProvision() {
    return groupOfUsersToProvision;
  }

  /**
   * group of users to provision
   * @param groupOfUsersToProvision
   * @return this for chaining
   */
  public DatadogProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }

}

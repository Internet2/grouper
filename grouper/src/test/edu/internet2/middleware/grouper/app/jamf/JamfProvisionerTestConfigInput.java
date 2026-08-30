package edu.internet2.middleware.grouper.app.jamf;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;

/**
 * Builder-pattern config input for Jamf provisioner tests.
 */
public class JamfProvisionerTestConfigInput {

  /** provisioner config id (required) */
  private String configId = null;

  /** extra config by suffix and value (overrides defaults) */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /** optional group to restrict which entities get provisioned */
  private Group groupOfUsersToProvision;

  public JamfProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  public String getConfigId() {
    return configId;
  }

  public JamfProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  public Group getGroupOfUsersToProvision() {
    return groupOfUsersToProvision;
  }

  public JamfProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }

}

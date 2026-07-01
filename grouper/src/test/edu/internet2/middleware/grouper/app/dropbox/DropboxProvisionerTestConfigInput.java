package edu.internet2.middleware.grouper.app.dropbox;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;

/**
 * Builder-style config input for Dropbox provisioner tests.
 */
public class DropboxProvisionerTestConfigInput {

  /** provisioner config id (required) */
  private String configId = null;

  /**
   * @param string the provisioner config id
   * @return this for chaining
   */
  public DropboxProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * @return the provisioner config id
   */
  public String getConfigId() {
    return configId;
  }

  /** extra config by suffix and value (overrides defaults) */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  /**
   * @param suffix config suffix (after provisioner.&lt;configId&gt;.)
   * @param value config value
   * @return this for chaining
   */
  public DropboxProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  /**
   * @return the extra config map
   */
  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

  /** optional group to restrict which users get provisioned */
  private Group groupOfUsersToProvision;

  /**
   * @return the group of users to provision
   */
  public Group getGroupOfUsersToProvision() {
    return groupOfUsersToProvision;
  }

  /**
   * @param groupOfUsersToProvision group of users to provision
   * @return this for chaining
   */
  public DropboxProvisionerTestConfigInput assignGroupOfUsersToProvision(Group groupOfUsersToProvision) {
    this.groupOfUsersToProvision = groupOfUsersToProvision;
    return this;
  }

}

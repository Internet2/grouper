package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder-pattern config input for AssetSonar provisioner tests.
 */
public class AssetSonarProvisionerTestConfigInput {

  /** provisioner config id */
  private String configId = "myAssetSonarProvisioner";

  /** extra config by suffix and value (overrides defaults) */
  private Map<String, String> extraConfig = new HashMap<String, String>();

  public AssetSonarProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  public String getConfigId() {
    return configId;
  }

  public AssetSonarProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
    this.extraConfig.put(suffix, value);
    return this;
  }

  public Map<String, String> getExtraConfig() {
    return this.extraConfig;
  }

}

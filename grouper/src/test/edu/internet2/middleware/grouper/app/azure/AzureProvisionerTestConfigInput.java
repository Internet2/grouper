package edu.internet2.middleware.grouper.app.azure;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author mchyzer
 *
 */
public class AzureProvisionerTestConfigInput {
  
  private boolean realAzure = false;
  
  /**
   * 
   * @param theRealAzure
   * @return this for chaining
   */
  public AzureProvisionerTestConfigInput assignRealAzure(boolean theRealAzure) {
    this.realAzure = theRealAzure;
    return this;
  }
  
  private String displayNameMapping = "name";
  
  
  
  public boolean isRealAzure() {
    return realAzure;
  }


  public String getDisplayNameMapping() {
    return displayNameMapping;
  }

  
  public AzureProvisionerTestConfigInput assignDisplayNameMapping(String displayNameMapping) {
    this.displayNameMapping = displayNameMapping;
    return this;
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
  public AzureProvisionerTestConfigInput addExtraConfig(String suffix, String value) {
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
   * 5 (default), or 9
   */
  public int getGroupAttributeCount() {
    return groupAttributeCount;
  }

  /**
   * 5 (default), or 9
   */
  public AzureProvisionerTestConfigInput assignGroupAttributeCount(int groupAttributeCount) {
    this.groupAttributeCount = groupAttributeCount;
    return this;
  }

  /**
   * default to myAzureProvisioner
   */
  private String configId = "myAzureProvisioner";

  /**
   * 5 (default), or 9
   */
  private int groupAttributeCount = 5;
  
  /**
   * default to myAzureProvisioner
   * @param string
   * @return this for chaining
   */
  public AzureProvisionerTestConfigInput assignConfigId(String string) {
    this.configId = string;
    return this;
  }

  /**
   * default to myAzureProvisioner
   * @return config id
   */
  public String getConfigId() {
    return configId;
  }
  
}

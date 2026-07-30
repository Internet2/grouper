package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GuiUserLifecyclePolicy {
  
  private String configId;

  private String policyName;
  
  private String description;
  
  private Integer index;

  public String getConfigId() {
    return configId;
  }
  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public String getPolicyName() {
    return policyName;
  }

  
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  
  public String getDescription() {
    return description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public Integer getIndex() {
    return index;
  }

  
  public void setIndex(Integer index) {
    this.index = index;
  }

  public static GuiUserLifecyclePolicy convertFromUserLifecyclePolicyConfiguration(UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration) {
    
    GuiUserLifecyclePolicy guiUserLifecyclePolicy = new GuiUserLifecyclePolicy();
    guiUserLifecyclePolicy.setConfigId(userLifecyclePolicyConfiguration.getConfigId());
    guiUserLifecyclePolicy.setPolicyName(userLifecyclePolicyConfiguration.retrieveAttributeValueFromConfig("name", true));
    guiUserLifecyclePolicy.setDescription(userLifecyclePolicyConfiguration.retrieveAttributeValueFromConfig("description", true));
    guiUserLifecyclePolicy.setIndex(GrouperUtil.intObjectValue(userLifecyclePolicyConfiguration.retrieveAttributeValueFromConfig("index", true), true));
    
    return guiUserLifecyclePolicy;
  }

}

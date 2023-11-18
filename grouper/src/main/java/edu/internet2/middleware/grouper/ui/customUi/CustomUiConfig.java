package edu.internet2.middleware.grouper.ui.customUi;

import java.util.ArrayList;
import java.util.List;

public class CustomUiConfig {

  private String groupUUIDOrName;
  
  private boolean enabled;
  
  private boolean externalizedText;
  
  private List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeans = new ArrayList<CustomUiUserQueryConfigBean>();
  
  private List<CustomUiTextConfigBean> customUiTextConfigBeans = new ArrayList<CustomUiTextConfigBean>();

  
  public String getGroupUUIDOrName() {
    return groupUUIDOrName;
  }

  
  public void setGroupUUIDOrName(String groupUUIDOrName) {
    this.groupUUIDOrName = groupUUIDOrName;
  }

  
  public boolean isEnabled() {
    return enabled;
  }

  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  
  public boolean isExternalizedText() {
    return externalizedText;
  }

  
  public void setExternalizedText(boolean externalizedText) {
    this.externalizedText = externalizedText;
  }

  
  public List<CustomUiUserQueryConfigBean> getCustomUiUserQueryConfigBeans() {
    return customUiUserQueryConfigBeans;
  }

  
  public void setCustomUiUserQueryConfigBeans(
      List<CustomUiUserQueryConfigBean> customUiUserQueryConfigBeans) {
    this.customUiUserQueryConfigBeans = customUiUserQueryConfigBeans;
  }

  
  public List<CustomUiTextConfigBean> getCustomUiTextConfigBeans() {
    return customUiTextConfigBeans;
  }

  
  public void setCustomUiTextConfigBeans(
      List<CustomUiTextConfigBean> customUiTextConfigBeans) {
    this.customUiTextConfigBeans = customUiTextConfigBeans;
  }
  
}

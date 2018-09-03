package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

public abstract class GrouperTemplateLogicBase {

  private String stemId;
  
  private String systemNameExtension;
  
  private String friendlyNameExtension;

  public String getStemId() {
    return stemId;
  }

  public void setStemId(String stemId) {
    this.stemId = stemId;
  }

  public String getSystemNameExtension() {
    return systemNameExtension;
  }

  public void setSystemNameExtension(String systemNameExtension) {
    this.systemNameExtension = systemNameExtension;
  }

  public String getFriendlyNameExtension() {
    return friendlyNameExtension;
  }

  public void setFriendlyNameExtension(String friendlyNameExtension) {
    this.friendlyNameExtension = friendlyNameExtension;
  }
  
  
  public abstract List<ServiceAction> displayOnScreen();
  
  
}

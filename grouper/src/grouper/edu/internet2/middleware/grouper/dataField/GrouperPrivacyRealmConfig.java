package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class GrouperPrivacyRealmConfig {
  
  private String privacyRealmName;
  
  private boolean privacyRealmPublic;
  
  private boolean privacyRealmAuthenticated;
  
  private boolean privacyRealmSysadminsCanView;
  
  private String privacyRealmViewersGroupName;
  
  private String privacyRealmUpdatersGroupName;

  private String privacyRealmReadersGroupName;
  
  private String configId;
  
  public void readFromConfig(String configId) {
    
    this.configId = configId;
    
    this.privacyRealmName = GrouperConfig.retrieveConfig().propertyValueString("grouperPrivacyRealm." + configId + ".privacyRealmName");
    
    this.privacyRealmPublic = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperPrivacyRealm." + configId + ".privacyRealmPublic", false);
    
    this.privacyRealmAuthenticated = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperPrivacyRealm." + configId + ".privacyRealmAuthenticated", false);

    this.privacyRealmSysadminsCanView = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperPrivacyRealm." + configId + ".privacyRealmSysadminsCanView", true);

    this.privacyRealmViewersGroupName = GrouperConfig.retrieveConfig().propertyValueString("grouperPrivacyRealm." + configId + ".privacyRealmViewersGroupName");
    
    this.privacyRealmUpdatersGroupName = GrouperConfig.retrieveConfig().propertyValueString("grouperPrivacyRealm." + configId + ".privacyRealmUpdatersGroupName");

    this.privacyRealmReadersGroupName = GrouperConfig.retrieveConfig().propertyValueString("grouperPrivacyRealm." + configId + ".privacyRealmReadersGroupName");
    
  }
  
  
  public String getConfigId() {
    return configId;
  }

  
  public String getPrivacyRealmName() {
    return privacyRealmName;
  }

  
  public boolean isPrivacyRealmPublic() {
    return privacyRealmPublic;
  }

  
  public boolean isPrivacyRealmAuthenticated() {
    return privacyRealmAuthenticated;
  }

  
  public boolean isPrivacyRealmSysadminsCanView() {
    return privacyRealmSysadminsCanView;
  }

  
  public String getPrivacyRealmViewersGroupName() {
    return privacyRealmViewersGroupName;
  }


  
  public String getPrivacyRealmUpdatersGroupName() {
    return privacyRealmUpdatersGroupName;
  }


  
  public String getPrivacyRealmReadersGroupName() {
    return privacyRealmReadersGroupName;
  }
  
  
  
  
  
}

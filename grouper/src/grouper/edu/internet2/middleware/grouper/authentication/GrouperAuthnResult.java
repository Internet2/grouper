package edu.internet2.middleware.grouper.authentication;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class GrouperAuthnResult {

  public GrouperAuthnResult() {
    
  }

  private String configId;
  
  private boolean authenticated;
  
  private Timestamp timestamp;
  
  private String remoteUser;

  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public boolean isAuthenticated() {
    return authenticated;
  }

  
  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
  }

  
  public Timestamp getTimestamp() {
    return timestamp;
  }

  
  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  
  public String getRemoteUser() {
    return remoteUser;
  }

  
  public void setRemoteUser(String remoteUser) {
    this.remoteUser = remoteUser;
  }

  /**
   * 
   * @return true if the user is authenticated and the authn time is within the authn timeout/expiration
   */
  public boolean isAuthenticatedInTime() {
    if (!this.authenticated) {
      return false;
    }
    
    boolean enabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem." 
        + this.configId + ".enabled", true);
    
    if (!enabled) {
      return false;
    }
    
    int authnTimeoutSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.oidcExternalSystem." 
        + this.configId + ".authnTimeoutSeconds", -1);
    
    if (authnTimeoutSeconds < 0) {
      return true;
    }
    long now = System.currentTimeMillis();
    long authnTime = this.timestamp.getTime();
    long diff = now - authnTime;
    if (diff < (authnTimeoutSeconds * 1000)) {
      return true;
    }
    return false;
    
  }
  
  
}

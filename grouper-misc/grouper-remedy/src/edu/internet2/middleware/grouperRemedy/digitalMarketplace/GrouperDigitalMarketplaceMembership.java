package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * grouper digital marketplace membership
 * @author mchyzer
 *
 */
public class GrouperDigitalMarketplaceMembership {

  /**
   * group extension
   */
  private String groupName;
  
  /**
   * group extension
   * @return the statusString
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * group extension
   * @param statusString1 the statusString to set
   */
  public void setGroupName(String statusString1) {
    this.groupName = statusString1;
  }

  /**
   * Multikey is groupName, netId
   * @return box api connection never null
   */
  public synchronized static Map<MultiKey, GrouperDigitalMarketplaceMembership> retrieveMemberships() {
    
    return GrouperDigitalMarketplaceUser.retrieveDigitalMarketplaceMemberships();

  }

  /**
   * netId
   */
  private String loginName;
  
  /**
   * netId
   * @return the netId
   */
  public String getLoginName() {
    return this.loginName;
  }
  
  /**
   * netId
   * @param netId the userName to set
   */
  public void setLoginName(String netId) {
    this.loginName = netId;
  }
  
}

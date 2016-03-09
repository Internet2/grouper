/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasUserContainer;


/**
 *
 */
public class AsasApiUser {

  /**
   * 
   */
  public AsasApiUser() {
  }

  private String id;
  
  private String tierNetId;
  
  private String userName;

  private AsasApiName name;

  
  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * @return the tierNetId
   */
  public String getTierNetId() {
    return this.tierNetId;
  }

  
  /**
   * @param tierNetId the tierNetId to set
   */
  public void setTierNetId(String tierNetId) {
    this.tierNetId = tierNetId;
  }

  
  /**
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  }

  
  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  
  /**
   * @return the name
   */
  public AsasApiName getName() {
    return this.name;
  }

  
  /**
   * @param name the name to set
   */
  public void setName(AsasApiName name) {
    this.name = name;
  }
  
  /**
   * convert the api beans to the transport beans
   * @param asasApiUser
   * @return the api bean
   */
  public static AsasUserContainer convertToAsasUserContainer(AsasApiUser asasApiUser) {
    if (asasApiUser == null) {
      return null;
    }
    AsasUserContainer asasUserContainer = new AsasUserContainer();
    asasUserContainer.setId(asasApiUser.getId());
    asasUserContainer.setName(AsasApiName.convertToAsasName(asasApiUser.getName()));
    asasUserContainer.setTierNetId(asasApiUser.getTierNetId());
    asasUserContainer.setUserName(asasApiUser.getUserName());
    return asasUserContainer;
  }

}

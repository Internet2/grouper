/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * Multiple groups
 * @author mchyzer
 *
 */
public class AsasUserContainer extends AsasResponseBeanBase {

  public AsasUserContainer() {
    super();
    this.getMeta().setResourceType("User");
  }
  
  private String id;
  
  private String tierNetId;

  private String userName;

  private AsasName name;
  
  
  
  
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
  public AsasName getName() {
    return this.name;
  }



  
  /**
   * @param name the name to set
   */
  public void setName(AsasName name) {
    this.name = name;
  }



  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    AsasUserContainer asasUserContainer = new AsasUserContainer();
    asasUserContainer.setId("whatevs");
    
    String string = StandardApiServerUtils.indent(AsasRestContentType.json.writeString(asasUserContainer), true);
    
    System.out.println(string);
    
    
  }
  
  
  
}

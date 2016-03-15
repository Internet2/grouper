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

  /**
   * constructor
   */
  public AsasUserContainer() {
    super();
    this.getMeta().setResourceType("User");
  }

  /**
   * user id
   */
  private String id;
  
  /**
   * tierNetId
   */
  private String tierNetId;

  /**
   * userName
   */
  private String userName;

  /**
   * name
   */
  private AsasName name;
  
  
  
  
  /**
   * @return the id
   */
  public String getId() {
    return this.id;
  }



  
  /**
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }



  
  /**
   * @return the tierNetId
   */
  public String getTierNetId() {
    return this.tierNetId;
  }



  
  /**
   * @param tierNetId1 the tierNetId to set
   */
  public void setTierNetId(String tierNetId1) {
    this.tierNetId = tierNetId1;
  }



  
  /**
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  }



  
  /**
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1) {
    this.userName = userName1;
  }



  
  /**
   * @return the name
   */
  public AsasName getName() {
    return this.name;
  }



  
  /**
   * @param name1 the name to set
   */
  public void setName(AsasName name1) {
    this.name = name1;
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

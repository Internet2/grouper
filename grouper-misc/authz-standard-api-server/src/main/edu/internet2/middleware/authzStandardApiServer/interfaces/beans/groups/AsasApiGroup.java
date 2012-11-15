/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroup;


/**
 * Group in authz standard api
 * @author mchyzer
 *
 */
public class AsasApiGroup {
  
  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasGroup convertTo(AsasApiGroup asasApiGroup) {
    if (asasApiGroup == null) {
      return null;
    }
    AsasGroup asasGroup = new AsasGroup();
    asasGroup.setDescription(asasApiGroup.getDescription());
    asasGroup.setDisplayName(asasApiGroup.getDisplayName());
    asasGroup.setId(asasApiGroup.getId());
    asasGroup.setName(asasApiGroup.getName());
    asasGroup.setStatus(asasApiGroup.getStatus());
    return asasGroup;
  }

  /** id of the group */
  private String id;
  
  /** name of group */
  private String name;
  
  /** display name of group */
  private String displayName;

  /** description of group */
  private String description;
  
  /** status: active or inactive */
  private String status;
  
  /**
   * id of the group
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * id of the group
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * name of group
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * name of group
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  
  /**
   * display name of group
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  
  /**
   * display name of group
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  
  /**
   * description of group
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * description of group
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  
  /**
   * status: active or inactive
   * @return the status
   */
  public String getStatus() {
    return this.status;
  }

  
  /**
   * status: active or inactive
   * @param status1 the status to set
   */
  public void setStatus(String status1) {
    this.status = status1;
  }
  
  
  
}

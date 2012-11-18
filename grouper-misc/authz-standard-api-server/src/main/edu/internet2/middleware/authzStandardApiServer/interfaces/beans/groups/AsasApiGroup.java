/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroup;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerConfig;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServer.version.AsasWsVersion;


/**
 * Group in authz standard api
 * @author mchyzer
 *
 */
public class AsasApiGroup {

  /**
   * path separator is a character that separates folders
   */
  private String pathSeparator;
    
  /**
   * path separator is a character that separates folders
   * @return the pathSeparator
   */
  public String getPathSeparator() {
    return pathSeparator;
  }

  /**
   * path separator is a character that separates folders
   * @param pathSeparator the pathSeparator to set
   */
  public void setPathSeparator(String pathSeparator) {
    this.pathSeparator = pathSeparator;
  }

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static List<AsasGroup> convertToList(Collection<AsasApiGroup> asasApiGroups) {
    
    if (asasApiGroups == null) {
      return null;
    }
    List<AsasGroup> asasGroups = new ArrayList<AsasGroup>();
    
    for (AsasApiGroup asasApiGroup : asasApiGroups) {
      AsasGroup asasGroup = convertTo(asasApiGroup);
      asasGroups.add(asasGroup);
    }
    
    return asasGroups;
  }

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
    
    String groupUriBase = StandardApiServerUtils.servletUrl() + "/" 
        + AsasWsVersion.retrieveCurrentClientVersion().name() + "/groups/name" 
        + StandardApiServerUtils.escapeUrlEncode(":")
        + StandardApiServerUtils.escapeUrlEncode(asasApiGroup.getName());
    
    String groupUriSuffix = "." + AsasRestContentType.retrieveContentType().name() + "?pathSeparator=" 
        + StandardApiServerUtils.escapeUrlEncode(StandardApiServerConfig.retrieveConfig().configItemPathSeparatorChar());
    
    asasGroup.setUri(groupUriBase + groupUriSuffix);

    //asasGroup.setAdminsUri(groupUriBase + "/");

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

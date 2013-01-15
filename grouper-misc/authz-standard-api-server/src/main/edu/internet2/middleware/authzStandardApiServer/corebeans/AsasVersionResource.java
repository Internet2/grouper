package edu.internet2.middleware.authzStandardApiServer.corebeans;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServer.version.AsasWsVersion;

/**
 * version resource
 * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
 * 
 * @author mchyzer
 *
 */
public class AsasVersionResource {
 
  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   */
  public AsasVersionResource() {
    
    this.groupsUri = "/" + AsasWsVersion.retrieveCurrentClientVersion() + "/groups." + AsasRestContentType.retrieveContentType();
    this.foldersUri = "/" + AsasWsVersion.retrieveCurrentClientVersion() + "/folders." + AsasRestContentType.retrieveContentType();
    this.entitiesUri = "/" + AsasWsVersion.retrieveCurrentClientVersion() + "/entities." + AsasRestContentType.retrieveContentType();
    this.permissionsUri = "/" + AsasWsVersion.retrieveCurrentClientVersion() + "/permissions." + AsasRestContentType.retrieveContentType();
    
  }
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/groups
   */
  private String groupsUri;

  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/folders
   */
  private String foldersUri;
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/entities
   */
  private String entitiesUri;

  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/permissions
   */
  private String permissionsUri;
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/groups
   * @return the groupsUri
   */
  public String getGroupsUri() {
    return this.groupsUri;
  }

  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/groups
   * @param groupsUri1 the groupsUri to set
   */
  public void setGroupsUri(String groupsUri1) {
    this.groupsUri = groupsUri1;
  }
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/folders
   * @return the foldersUri
   */
  public String getFoldersUri() {
    return this.foldersUri;
  }

  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/folders
   * @param foldersUri1 the foldersUri to set
   */
  public void setFoldersUri(String foldersUri1) {
    this.foldersUri = foldersUri1;
  }
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/entities
   * @return the entitiesUri
   */
  public String getEntitiesUri() {
    return this.entitiesUri;
  }
  
  /**
   * https://groups.institution.edu/groupsApp/authzStandardApi/v1/entities
   * @param entitiesUri1 the entitiesUri to set
   */
  public void setEntitiesUri(String entitiesUri1) {
    this.entitiesUri = entitiesUri1;
  }
  
  /**
   * @return the permissionsUri
   */
  public String getPermissionsUri() {
    return this.permissionsUri;
  }
  
  /**
   * @param permissionsUri1 the permissionsUri to set
   */
  public void setPermissionsUri(String permissionsUri1) {
    this.permissionsUri = permissionsUri1;
  }
  
}

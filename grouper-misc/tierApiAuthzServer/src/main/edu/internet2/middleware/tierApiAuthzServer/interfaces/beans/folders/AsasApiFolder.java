/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.tierApiAuthzServer.contentType.AsasRestContentType;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasFolder;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.tierApiAuthzServer.version.TaasWsVersion;


/**
 * Group in authz standard api
 * @author mchyzer
 *
 */
public class AsasApiFolder {

  /**
   * convert the api beans to the transport beans
   * @param asasApiFolders
   * @return the api bean
   */
  public static List<AsasFolder> convertToList(Collection<AsasApiFolder> asasApiFolders) {
    
    if (asasApiFolders == null) {
      return null;
    }
    List<AsasFolder> asasFolders = new ArrayList<AsasFolder>();
    
    for (AsasApiFolder asasApiFolder : asasApiFolders) {
      AsasFolder asasFolder = convertToAsasFolder(asasApiFolder);
      asasFolders.add(asasFolder);
    }
    
    return asasFolders;
  }

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasFolder convertToAsasFolder(AsasApiFolder asasApiFolder) {
    if (asasApiFolder == null) {
      return null;
    }
    AsasFolder asasFolder = new AsasFolder();
    asasFolder.setDescription(asasApiFolder.getDescription());
    asasFolder.setDisplayName(asasApiFolder.getDisplayName());
    asasFolder.setId(asasApiFolder.getId());
    asasFolder.setName(asasApiFolder.getName());
    asasFolder.setStatus(asasApiFolder.getStatus());
    
    String folderUriBase = "/" 
        + TaasWsVersion.retrieveCurrentClientVersion().name() + "/folders/name" 
        + StandardApiServerUtils.escapeUrlEncode(":")
        + StandardApiServerUtils.escapeUrlEncode(asasApiFolder.getName());
    
    String folderUriSuffix = "." + AsasRestContentType.retrieveContentType().name();
    
    asasFolder.setUri(folderUriBase + folderUriSuffix);
    
    //if we are a the root, dont set this since it is null
    if (!StandardApiServerUtils.pathIsRootFolder(asasApiFolder.getName())) {
      asasFolder.setParentFolderUri("/" 
          + TaasWsVersion.retrieveCurrentClientVersion().name() + "/folders/name" 
          + StandardApiServerUtils.escapeUrlEncode(":")
          + StandardApiServerUtils.escapeUrlEncode(StandardApiServerUtils.pathParentFolderName(asasApiFolder.getName()) + folderUriSuffix));
    }

    asasFolder.setCreatorsUri(folderUriBase + "/creators" + folderUriSuffix);
    asasFolder.setAdminsUri(folderUriBase + "/admins" + folderUriSuffix);

    return asasFolder;
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

  /**
   * convert the transport beans to the api beans
   * @param asasApiGroups
   * @return the api bean
   */
  public static AsasApiFolder convertToAsasApiFolder(AsasFolder asasFolder) {
    if (asasFolder == null) {
      return null;
    }
    AsasApiFolder asasApiFolder = new AsasApiFolder();
    asasApiFolder.setDescription(asasFolder.getDescription());
    asasApiFolder.setDisplayName(asasFolder.getDisplayName());
    asasApiFolder.setId(asasFolder.getId());
    asasApiFolder.setName(asasFolder.getName());
    asasApiFolder.setStatus(asasFolder.getStatus());
     
    return asasApiFolder;
  }
  
  
  
}

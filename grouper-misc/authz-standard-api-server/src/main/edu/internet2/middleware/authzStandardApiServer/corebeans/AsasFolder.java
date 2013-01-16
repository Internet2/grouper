/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;


/**
 * @author mchyzer
 *
 */
public class AsasFolder {
  
  /** id */
  private String id;

  /** name */
  private String name;
  
  /** displayName */
  private String displayName;

  /** description */
  private String description;
  
  /** status (active/inactive) */
  private String status;
  
  /** admins uri */
  private String adminsUri;
  
  /** folderCreators uri */
  private String creatorsUri;
  
  /** reference to the parent folder: URI.  */
  private String parentFolderUri;

  /** uri of this folder */
  private String uri;

  
  /**
   * id
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * id
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  
  /**
   * admins uri
   * @return the adminsUri
   */
  public String getAdminsUri() {
    return this.adminsUri;
  }
  
  /**
   * admins uri
   * @param adminsUri1 the adminsUri to set
   */
  public void setAdminsUri(String adminsUri1) {
    this.adminsUri = adminsUri1;
  }

  /**
   * name
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * name
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  
  /**
   * display name
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  
  /**
   * display name
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  
  /**
   * description
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * description
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  
  /**
   * active/inactive
   * @return the status
   */
  public String getStatus() {
    return this.status;
  }

  
  /**
   * active/inactive
   * @param status1 the status to set
   */
  public void setStatus(String status1) {
    this.status = status1;
  }
  
  /**
   * uri to entities who can create objects in this folder
   * @return the creatorsUri
   */
  public String getCreatorsUri() {
    return this.creatorsUri;
  }
  
  /**
   * uri to entities who can create objects in this folder
   * @param creatorsUri the creatorsUri to set
   */
  public void setCreatorsUri(String folderCreatorsUri1) {
    this.creatorsUri = folderCreatorsUri1;
  }

  
  /**
   * uri of the parent folder of this folder
   * @return the parentFolderUri
   */
  public String getParentFolderUri() {
    return this.parentFolderUri;
  }

  
  /**
   * uri to entities who can create objects in this folder
   * @param parentFolderUri1 the parentFolderUri to set
   */
  public void setParentFolderUri(String parentFolderUri1) {
    this.parentFolderUri = parentFolderUri1;
  }


  /**
   * uri of this folder
   * @return the uri
   */
  public String getUri() {
    return this.uri;
  }

  /**
   * uri of this folder
   * @param uri1 the uri to set
   */
  public void setUri(String uri1) {
    this.uri = uri1;
  }

}

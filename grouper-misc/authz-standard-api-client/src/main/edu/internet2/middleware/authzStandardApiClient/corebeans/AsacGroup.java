/**
 * 
 */
package edu.internet2.middleware.authzStandardApiClient.corebeans;


/**
 * Group in authz standard api
 * @author mchyzer
 *
 */
public class AsacGroup {
  
  /** reference to the parent folder: URI.  */
  private String parentFolderUri;

  
  /**
   * reference to the parent folder: URI.
   * @return the parentFolderUri
   */
  public String getParentFolderUri() {
    return this.parentFolderUri;
  }



  
  /**
   * reference to the parent folder: URI.
   * @param parentFolderUri1 the parentFolderUri to set
   */
  public void setParentFolderUri(String parentFolderUri1) {
    this.parentFolderUri = parentFolderUri1;
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
  
  /** uri of this group */
  private String uri;

  /** admins uri */
  private String adminsUri;
  
  /** updaters uri */
  private String updatersUri;
  
  /** readers uri */
  private String readersUri;
  
  /** optins uri */
  private String optinsUri;
  
  /** optouts uri */
  private String optoutsUri;
  
  /** direct members URL */
  private String membersUri;

  
  
  
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
   * updaters uri
   * @return the updatersUri
   */
  public String getUpdatersUri() {
    return this.updatersUri;
  }


  
  /**
   * updaters uri
   * @param updatersUri1 the updatersUri to set
   */
  public void setUpdatersUri(String updatersUri1) {
    this.updatersUri = updatersUri1;
  }


  
  /**
   * readers uri
   * @return the readersUri
   */
  public String getReadersUri() {
    return this.readersUri;
  }


  
  /**
   * readers uri
   * @param readersUri1 the readersUri to set
   */
  public void setReadersUri(String readersUri1) {
    this.readersUri = readersUri1;
  }


  
  /**
   * optins uri
   * @return the optinsUri
   */
  public String getOptinsUri() {
    return this.optinsUri;
  }


  
  /**
   * optins uri
   * @param optinsUri1 the optinsUri to set
   */
  public void setOptinsUri(String optinsUri1) {
    this.optinsUri = optinsUri1;
  }


  
  /**
   * optouts uri
   * @return the optoutsUri
   */
  public String getOptoutsUri() {
    return this.optoutsUri;
  }


  
  /**
   * optouts uri
   * @param optoutsUri1 the optoutsUri to set
   */
  public void setOptoutsUri(String optoutsUri1) {
    this.optoutsUri = optoutsUri1;
  }


  
  /**
   * members uri
   * @return the membersUri
   */
  public String getMembersUri() {
    return this.membersUri;
  }


  
  /**
   * members uri
   * @param membersUri1 the membersUri to set
   */
  public void setMembersUri(String membersUri1) {
    this.membersUri = membersUri1;
  }


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
   * uri of this group
   * @return the uri
   */
  public String getUri() {
    return this.uri;
  }

  
  /**
   * uri of this group
   * @param uri1 the uri to set
   */
  public void setUri(String uri1) {
    this.uri = uri1;
  }
  
  
  
}

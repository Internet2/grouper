package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.GrouperPassword.Application;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.j2ee.Authentication;

/**
 * <p>Use this class to add username and password in grouper registry</p>
 * <p>Sample call to create a username password for grouper ui
 * 
 * <blockquote>
 * <pre>
 * new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignEntityType("username")
 *  .assignApplication(GrouperPassword.Application.UI).save();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to create a username password for grouper webservices
 * <blockquote>
 * <pre>
 * new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignEntityType("username")
 *  .assignApplication(GrouperPassword.Application.WS).save();
 * </pre>
 * </blockquote>
 * </p>
 *
 */
public class GrouperPasswordSave {
  
  /**
   * username to be assigned
   */
  private String username;
  
  /**
   * entity type to be assigned
   */
  private String entityType;
  
  /**
   * 
   */
  private EncryptionType encryptionType;
  
  private String thePassword;
  
  private Application application;
  
  /**
   * save credentials into the database
   */
  public void save() {
    new Authentication().assignUserPassword(this);
  }
  
  /**
   * assign username to be stored
   * @param username
   * @return
   */
  public GrouperPasswordSave assignUsername(String username) {
    this.username = username;
    return this;
  }
  
  /**
   * assign entity type e.g. username
   * @param entityType
   * @return
   */
  public GrouperPasswordSave assignEntityType(String entityType) {
    this.entityType = entityType;
    return this;
  }
  
  /**
   * assign encryption type
   * @param encryptionType
   * @return
   */
  public GrouperPasswordSave assignEncryptionType(EncryptionType encryptionType) {
    this.encryptionType = encryptionType;
    return this;
  }
  
  /**
   * assign password to be stored
   * @param password
   * @return
   */
  public GrouperPasswordSave assignPassword(String password) {
    this.thePassword = password;
    return this;
  }
  
  /**
   * assign application type (ws or ui)
   * @param application
   * @return
   */
  public GrouperPasswordSave assignApplication(GrouperPassword.Application application) {
    this.application = application;
    return this;
  }


  
  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }


  
  /**
   * @return the entityType
   */
  public String getEntityType() {
    return entityType;
  }

  
  /**
   * @return the encryptionType
   */
  public EncryptionType getEncryptionType() {
    return encryptionType;
  }


  
  /**
   * @return the thePassword
   */
  public String getThePassword() {
    return thePassword;
  }


  
  /**
   * @return the application
   */
  public Application getApplication() {
    return application;
  }
  
  
}

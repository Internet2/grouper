package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.GrouperPassword.Application;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;

public class GrouperPasswordSave {
  
  private String username;
  
  private String entityType;
  
  private EncryptionType encryptionType;
  
  private String thePassword;
  
  private Application application;
  
  public GrouperPasswordSave assignUsername(String username) {
    this.username = username;
    return this;
  }
  
  
  public GrouperPasswordSave assignEntityType(String entityType) {
    this.entityType = entityType;
    return this;
  }
  
  public GrouperPasswordSave assignEncryptionType(EncryptionType encryptionType) {
    this.encryptionType = encryptionType;
    return this;
  }
  
  
  public GrouperPasswordSave assignPassword(String password) {
    this.thePassword = password;
    return this;
  }
  
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

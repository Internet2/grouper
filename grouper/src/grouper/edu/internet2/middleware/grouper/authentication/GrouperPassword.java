/**
 * 
 */
package edu.internet2.middleware.grouper.authentication;


import org.apache.commons.codec.digest.DigestUtils;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * grouper password object to store credentials
 * @author vsachdeva
 * 
 */
@SuppressWarnings("serial")
public class GrouperPassword extends GrouperAPI implements Hib3GrouperVersioned {
  
  
  public GrouperPassword() {
    this.id = GrouperUuid.getUuid();
  }

  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** username */
  public static final String COLUMN_USER_NAME = "username";

  /** member_id */
  public static final String COLUMN_MEMBER_ID = "member_id";
  
  /** entity type */
  public static final String COLUMN_ENTITY_TYPE = "entity_type";
  
  /** is_hashed */
  public static final String COLUMN_IS_HASHED = "is_hashed";
  
  /** encryption type */
  public static final String COLUMN_ENCRYPTION_TYPE = "encryption_type";
  
  /** salt */
  public static final String COLUMN_SALT = "the_salt";
  
  /** password */
  public static final String COLUMN_PASSWORD = "the_password";
  
  /** ws or ui */
  public static final String COLUMN_APPLICATION = "application";
  
  /** allowed from cidrs */
  public static final String COLUMN_ALLOWED_FROM_CIDRS = "allowed_from_cidrs";
  
  /** recent source addresses */
  public static final String COLUMN_RECENT_SOURCE_ADDRESSES = "recent_source_addresses";
  
  /** failed source addresses */
  public static final String COLUMN_FAILED_SOURCE_ADDRESSES = "failed_source_addresses";
  
  /** last authenticated */
  public static final String COLUMN_LAST_AUTHENTICATED = "last_authenticated";
  
  /** last edited */
  public static final String COLUMN_LAST_EDITED = "last_edited";
  
  /** failed logins */
  public static final String COLUMN_FAILED_LOGINS = "failed_logins";
  
  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PASSWORD = "grouper_password";
  
  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: userName */
  public static final String FIELD_USER_NAME = "username";
  
  /** constant for field name for: memberId */
  public static final String FIELD_MEMBER_ID = "memberId";
  
  /** constant for field name for: entityType */
  public static final String FIELD_ENTITY_TYPE = "entityType";
  
  /** constant for field name for: isHashed */
  public static final String FIELD_IS_HASHED = "isHashed";
  
  /** constant for field name for: encryptionType */
  public static final String FIELD_ENCRYPTION_TYPE = "encryptionType";
  
  /** constant for field name for: theSalt */
  public static final String FIELD_THE_SALT = "theSalt";
  
  /** constant for field name for: thePassword */
  public static final String FIELD_THE_PASSWORD = "thePassword";
  
  /** constant for field name for: application */
  public static final String FIELD_APPLICATION = "application";
  
  /** constant for field name for: allowedFromCidrs */
  public static final String FIELD_ALLLOWED_FROM_CIDRS = "allowedFromCidrs";
  
  /** constant for field name for: recentSourceAddresses */
  public static final String FIELD_RECENT_SOURCE_ADDRESSES = "recentSourceAddresses";
  
  /** constant for field name for: failedSourceAddresses */
  public static final String FIELD_FAILED_SOURCE_ADDRESSES = "failedSourceAddresses";
  
  /** constant for field name for: lastAuthenticated */
  public static final String FIELD_LAST_AUTHENTICATED = "lastAuthenticated";
  
  /** constant for field name for: lastEdited */
  public static final String FIELD_LAST_EDITED = "lastEdited";
  
  /** constant for field name for: failedLogins */
  public static final String FIELD_FAILED_LOGINS = "failedLogins";
  
  private String id;
  
  private String username;

  private String memberId;
  
  private String entityType;
  
  private String isHashedDb;
  
  private boolean isHashed;
  
  private String encryptionTypeDb;
  
  private EncryptionType encryptionType;
  
  private String theSalt;
  
  private String thePassword;
  
  private String applicationDb;
  
  private Application application;

  private String allowedFromCidrs;
  
  private String recentSourceAddresses;
  
  private String failedSourceAddresses;
  
  private Long lastAuthenticated;
  
  private Long lastEdited;
  
  private String failedLogins;
  
  
  
  /**
   * @return the id
   */
  public String getId() {
    return id;
  }




  
  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }




  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }



  
  /**
   * @param userName the userName to set
   */
  public void setUsername(String username) {
    this.username = username;
  }






  
  
  
  /**
   * @return the memberId
   */
  public String getMemberId() {
    return memberId;
  }





  
  /**
   * @param memberId the memberId to set
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }





  /**
   * @return the entityType
   */
  public String getEntityType() {
    return entityType;
  }





  
  /**
   * @param entityType the entityType to set
   */
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }
  
  
  
  /**
   * @return the isHashedDb
   */
  public String getIsHashedDb() {
    return isHashedDb;
  }



  /**
   * @return the isHashed
   */
  public boolean isHashed() {
    return isHashed;
  }

 
  /**
   * @param isHashed the isHashed to set
   */
  public void setHashed(boolean isHashed) {
    this.isHashed = isHashed;
    this.isHashedDb = isHashed ? "T" : "F";
  }


  /**
   * @return the encryptionTypeDb
   */
  public String getEncryptionTypeDb() {
    return encryptionTypeDb;
  }




  /**
   * @return the encryptionType
   */
  public EncryptionType getEncryptionType() {
    return encryptionType;
  }



  
  /**
   * @param encryptionType the encryptionType to set
   */
  public void setEncryptionType(EncryptionType encryptionType) {
    this.encryptionType = encryptionType;
    this.encryptionTypeDb = encryptionType.name();
  }



  
  /**
   * @return the theSalt
   */
  public String getTheSalt() {
    return theSalt;
  }



  
  /**
   * @param theSalt the theSalt to set
   */
  public void setTheSalt(String theSalt) {
    this.theSalt = theSalt;
  }



  
  /**
   * @return the thePassword
   */
  public String getThePassword() {
    return thePassword;
  }



  
  /**
   * @param thePassword the thePassword to set
   */
  public void setThePassword(String thePassword) {
    this.thePassword = thePassword;
  }


  /**
   * @return the lastEdited
   */
  public Long getLastEdited() {
    return lastEdited;
  }



  
  /**
   * @param lastEdited the lastEdited to set
   */
  public void setLastEdited(Long lastEdited) {
    this.lastEdited = lastEdited;
  }


  
  
  /**
   * @return the applicationDb
   */
  public String getApplicationDb() {
    return applicationDb;
  }





  /**
   * @return the application
   */
  public Application getApplication() {
    return application;
  }



  
  /**
   * @param application the application to set
   */
  public void setApplication(Application application) {
    this.application = application;
    this.applicationDb = application.name();
  }
  


  
  /**
   * @return the allowedFromCidrs
   */
  public String getAllowedFromCidrs() {
    return allowedFromCidrs;
  }





  
  /**
   * @param allowedFromCidrs the allowedFromCidrs to set
   */
  public void setAllowedFromCidrs(String allowedFromCidrs) {
    this.allowedFromCidrs = allowedFromCidrs;
  }





  
  /**
   * @return the recentSourceAddresses
   */
  public String getRecentSourceAddresses() {
    return recentSourceAddresses;
  }





  
  /**
   * @param recentSourceAddresses the recentSourceAddresses to set
   */
  public void setRecentSourceAddresses(String recentSourceAddresses) {
    this.recentSourceAddresses = recentSourceAddresses;
  }





  
  /**
   * @return the failedSourceAddresses
   */
  public String getFailedSourceAddresses() {
    return failedSourceAddresses;
  }





  
  /**
   * @param failedSourceAddresses the failedSourceAddresses to set
   */
  public void setFailedSourceAddresses(String failedSourceAddresses) {
    this.failedSourceAddresses = failedSourceAddresses;
  }





  
  /**
   * @return the lastAuthenticated
   */
  public Long getLastAuthenticated() {
    return lastAuthenticated;
  }





  
  /**
   * @param lastAuthenticated the lastAuthenticated to set
   */
  public void setLastAuthenticated(Long lastAuthenticated) {
    this.lastAuthenticated = lastAuthenticated;
  }





  
  /**
   * @return the failedLogins
   */
  public String getFailedLogins() {
    return failedLogins;
  }





  
  /**
   * @param failedLogins the failedLogins to set
   */
  public void setFailedLogins(String failedLogins) {
    this.failedLogins = failedLogins;
  }
  




  
  /**
   * @param isHashedDb the isHashedDb to set
   */
  public void setIsHashedDb(String isHashedDb) {
    this.isHashedDb = isHashedDb;
  }





  
  /**
   * @param encryptionTypeDb the encryptionTypeDb to set
   */
  public void setEncryptionTypeDb(String encryptionTypeDb) {
    this.encryptionTypeDb = encryptionTypeDb;
    this.encryptionType = EncryptionType.valueOf(encryptionTypeDb);
  }





  
  /**
   * @param applicationDb the applicationDb to set
   */
  public void setApplicationDb(String applicationDb) {
    this.applicationDb = applicationDb;
  }







  public enum EncryptionType {
    
    SHA_256 {

      @Override
      public String generateHash(String input) {
        return DigestUtils.sha256Hex(input);
      }
      
    }, 
    
    RS_256 {

      @Override
      public String generateHash(String input) {
        return null;
      }
      
    };
    
    public abstract String generateHash(String input);
  }
  
  public enum Application {
    WS, UI
  }
  
  
  @Override
  public GrouperAPI clone() {
    // TODO Auto-generated method stub
    return null;
  }

}

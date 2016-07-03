/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v0;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;

/**
 *
 */
@GcPersistableClass(tableName="users", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdUserV0 implements AtlassianCwdUser {

  /**
   * id col
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private Long id;
  
  /**
   * id col
   * @return the id
   */
  public Long getId() {
    return this.id;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.userName == null) ? 0 : this.userName.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AtlassianCwdUserV0 other = (AtlassianCwdUserV0) obj;
    if (this.userName == null) {
      if (other.userName != null)
        return false;
    } else if (!this.userName.equals(other.userName))
      return false;
    return true;
  }

  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * user_name col
   */
  @GcPersistableField(columnName="name")
  private String userName;
  
  /**
   * user_name col
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  
  }

  /**
   * password col
   */
  @GcPersistableField
  private String password;
  
  /**
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }
  
  /**
   * @param password1 the password to set
   */
  public void setPassword(String password1) {
    this.password = password1;
  }

  /**
   * user_name col
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1) {
    this.userName = userName1;
  }

  /**
   * created date col
   */
  @GcPersistableField(columnName="created")
  private Timestamp createdDate;

  
  
  /**
   * created date col
   * @return the createdDate
   */
  public Timestamp getCreatedDate() {
    return this.createdDate;
  }

  
  /**
   * created date col
   * @param createdDate1 the createdDate to set
   */
  public void setCreatedDate(Timestamp createdDate1) {
    this.createdDate = createdDate1;
  }

  /**
   * display_name col
   */
  @GcPersistableField(columnName="fullname")
  private String displayName;

  
  /**
   * display_name col
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  
  /**
   * display_name col
   * @param displayName1 the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }


  /**
   * email_address col
   */
  @GcPersistableField(columnName="email")
  private String emailAddress;

  
  /**
   * email_address col
   * @return the emailAddress
   */
  public String getEmailAddress() {
    return this.emailAddress;
  }

  
  /**
   * email_address col
   * @param emailAddress1 the emailAddress to set
   */
  public void setEmailAddress(String emailAddress1) {
    this.emailAddress = emailAddress1;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdUser [id=" + this.id + ", userName=" + this.userName 
        + ", createdDate=" + this.createdDate
        + ", displayName=" + this.displayName
        + ", emailAddress=" + this.emailAddress + "]";
  }

  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

    Timestamp now = new Timestamp(System.currentTimeMillis());
    this.setCreatedDate(now);

  }
  
  /**
   * get all users
   * @return the users or null by map of username to user
   */
  public static Map<String, AtlassianCwdUser> retrieveUsers() {
    
    List<AtlassianCwdUserV0> resultList = new GcDbAccess().selectList(AtlassianCwdUserV0.class);
    Map<String, AtlassianCwdUser> resultMap = new LinkedHashMap<String, AtlassianCwdUser>();
    for (AtlassianCwdUserV0 atlassianCwdUser : resultList) {
      resultMap.put(atlassianCwdUser.getUserName(), atlassianCwdUser);
    }
    return resultMap;
  }

  /**
   * store this record insert or update
   */
  public void store() {
    new GcDbAccess().storeToDatabase(this);
  }

  /**
   * delete this record
   */
  public void delete() {
    new GcDbAccess().deleteFromDatabase(this);
  }


  /**
   * @see edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.id != null) {
      throw new RuntimeException("Why setting primary key if already exists! " + this.id);
    }
    Long maxId = new GcDbAccess().sql("select max(id) from users").select(Long.class);
    this.setId(maxId == null ? 0 : maxId + 1);
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getUpdatedDate()
   */
  public Timestamp getUpdatedDate() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setUpdatedDate(java.sql.Timestamp)
   */
  public void setUpdatedDate(Timestamp updatedDate1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getDirectoryId()
   */
  public Long getDirectoryId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerUserName()
   */
  public String getLowerUserName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerUserName(java.lang.String)
   */
  public void setLowerUserName(String lowerUserName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerDisplayName()
   */
  public String getLowerDisplayName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerDisplayName(java.lang.String)
   */
  public void setLowerDisplayName(String lowerDisplayName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getLowerEmailAddress()
   */
  public String getLowerEmailAddress() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setLowerEmailAddress(java.lang.String)
   */
  public void setLowerEmailAddress(String lowerEmailAddress1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getExternalId()
   */
  public String getExternalId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setExternalId(java.lang.String)
   */
  public void setExternalId(String externalId1) {
  }
  
}

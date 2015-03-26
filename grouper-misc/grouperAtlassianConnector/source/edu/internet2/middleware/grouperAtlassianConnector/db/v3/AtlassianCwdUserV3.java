/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v3;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser;
import edu.internet2.middleware.grouperAtlassianConnector.db.GrouperAtlassianDataReconcile;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;


/**
 *
 */
@GcPersistableClass(tableName="CWD_USER", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdUserV3 implements AtlassianCwdUser {

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
    AtlassianCwdUserV3 other = (AtlassianCwdUserV3) obj;
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
  @GcPersistableField
  private String userName;
  
  /**
   * user_name col
   * @return the userName
   */
  public String getUserName() {
    return this.userName;
  }

  
  /**
   * user_name col
   * @param userName1 the userName to set
   */
  public void setUserName(String userName1) {
    this.userName = userName1;
  }

  /**
   * active col
   */
  @GcPersistableField
  private Long active;

  /**
   * active col
   * @return the active
   */
  public Long getActive() {
    return this.active;
  }

  
  /**
   * active col
   * @param active1 the active to set
   */
  public void setActive(Long active1) {
    this.active = active1;
  }

  /**
   * updated_date col
   */
  @GcPersistableField
  private Timestamp updatedDate;

  /**
   * updated_date col
   * @return the updatedDate
   */
  public Timestamp getUpdatedDate() {
    return this.updatedDate;
  }

  
  /**
   * updated_date col
   * @param updatedDate1 the updatedDate to set
   */
  public void setUpdatedDate(Timestamp updatedDate1) {
    this.updatedDate = updatedDate1;
  }

  /**
   * directory_id col
   */
  @GcPersistableField
  private Long directoryId;
  
  /**
   * directory_id col
   * @return the directoryId
   */
  public Long getDirectoryId() {
    return this.directoryId;
  }
  
  /**
   * directory_id col
   * @param directoryId1 the directoryId to set
   */
  public void setDirectoryId(Long directoryId1) {
    this.directoryId = directoryId1;
  }

  /**
   * lower_user_name col
   */
  @GcPersistableField
  private String lowerUserName;

  
  
  /**
   * lower_user_name col
   * @return the lowerUserName
   */
  public String getLowerUserName() {
    return this.lowerUserName;
  }

  
  /**
   * lower_user_name col
   * @param lowerUserName1 the lowerUserName to set
   */
  public void setLowerUserName(String lowerUserName1) {
    this.lowerUserName = lowerUserName1;
  }

  /**
   * created date col
   */
  @GcPersistableField
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
   * lower_display_name col
   */
  @GcPersistableField
  private String lowerDisplayName;

  
  /**
   * lower_display_name col
   * @return the lowerDisplayName
   */
  public String getLowerDisplayName() {
    return this.lowerDisplayName;
  }

  
  /**
   * lower_display_name col
   * @param lowerDisplayName1 the lowerDisplayName to set
   */
  public void setLowerDisplayName(String lowerDisplayName1) {
    this.lowerDisplayName = lowerDisplayName1;
  }

  /**
   * lower_email_address col
   */
  @GcPersistableField
  private String lowerEmailAddress;

  
  /**
   * lower_email_address col
   * @return the lowerEmailAddress
   */
  public String getLowerEmailAddress() {
    return this.lowerEmailAddress;
  }

  
  /**
   * lower_email_address col
   * @param lowerEmailAddress1 the lowerEmailAddress to set
   */
  public void setLowerEmailAddress(String lowerEmailAddress1) {
    this.lowerEmailAddress = lowerEmailAddress1;
  }
  
  /**
   * display_name col
   */
  @GcPersistableField
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
  @GcPersistableField
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
   * external_id col
   */
  @GcPersistableField
  private String externalId;

  
  /**
   * external_id col
   * @return the externalId
   */
  public String getExternalId() {
    return this.externalId;
  }

  
  /**
   * external_id col
   * @param externalId1 the externalId to set
   */
  public void setExternalId(String externalId1) {
    this.externalId = externalId1;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdUser [id=" + this.id + ", userName=" + this.userName + ", active=" + this.active
        + ", updatedDate=" + this.updatedDate + ", directoryId=" + this.directoryId + ", lowerUserName="
        + this.lowerUserName + ", createdDate=" + this.createdDate + ", lowerDisplayName=" + this.lowerDisplayName
        + ", lowerEmailAddress=" + this.lowerEmailAddress + ", displayName=" + this.displayName
        + ", emailAddress=" + this.emailAddress + ", externalId=" + this.externalId + "]";
  }

  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

    this.setExternalId(UUID.randomUUID().toString());
    this.setActive(1L);
    Timestamp now = new Timestamp(System.currentTimeMillis());
    this.setCreatedDate(now);
    this.setUpdatedDate(now);
    this.setDirectoryId(GrouperAtlassianDataReconcile.directoryId());

  }
  
  /**
   * get all users
   * @return the users or null by map of username to user
   */
  public static Map<String, AtlassianCwdUser> retrieveUsers() {
    
    List<AtlassianCwdUserV3> resultList = new GcDbAccess().selectList(AtlassianCwdUserV3.class);
    Map<String, AtlassianCwdUser> resultMap = new LinkedHashMap<String, AtlassianCwdUser>();
    for (AtlassianCwdUserV3 atlassianCwdUser : resultList) {
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
    Long maxId = new GcDbAccess().sql("select max(id) from cwd_user").select(Long.class);
    this.setId(maxId + 1);
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return this.active == null ? null : this.active.equals(1L);
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdUser#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
    this.active = active1 == null ? null : (active1 ? 1L : 0L);
  }
  
}

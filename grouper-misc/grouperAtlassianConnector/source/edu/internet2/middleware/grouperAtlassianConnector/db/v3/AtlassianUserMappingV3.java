/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;


/**
 *
 */
@GcPersistableClass(tableName="APP_USER", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianUserMappingV3 implements AtlassianUserMapping {
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianUserMappingV3 [userKey_id=" + this.userKey + ", username=" + this.username 
        + ", lowerUsername=" + this.lowerUsername + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all user mappings
   * @return the user mappings or null by map of username to usermapping
   */
  public static Map<String, AtlassianUserMapping> retrieveUserMappings() {
    
    List<AtlassianUserMappingV3> resultList = new GcDbAccess().selectList(AtlassianUserMappingV3.class);
    Map<String, AtlassianUserMapping> resultMap = new LinkedHashMap<String, AtlassianUserMapping>();
    for (AtlassianUserMappingV3 atlassianUserMapping : resultList) {
      resultMap.put(atlassianUserMapping.getUsername(), atlassianUserMapping);
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
    if (this.userKey != null) {
      throw new RuntimeException("Why setting primary key if already exists! " + this.userKey);
    }
    
    Long maxId = new GcDbAccess().sql("select max(id) from app_user").select(Long.class);
    
    this.setUserKey("" + (maxId + 1));
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.userKey == null) ? 0 : this.userKey.hashCode());
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
    AtlassianUserMappingV3 other = (AtlassianUserMappingV3) obj;
    if (this.userKey == null) {
      if (other.userKey != null)
        return false;
    } else if (!this.userKey.equals(other.userKey))
      return false;
    return true;
  }

  /**
   * user key is primary key uuid
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true, columnName="ID")
  private String userKey;

  /**
   * user key is primary key uuid
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#getUserKey()
   */
  public String getUserKey() {
    return this.userKey;
  }


  /**
   * user key is primary key uuid
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#setUserKey(java.lang.String)
   */
  public void setUserKey(String userKey1) {
    this.userKey = userKey1;
  }

  /**
   * username is loginid
   */
  @GcPersistableField(columnName="USER_KEY")
  private String username;

  /**
   * username is loginid
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#getUsername()
   */
  public String getUsername() {
    return this.username;
  }


  /**
   * username is loginid
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#setUsername(java.lang.String)
   */
  public void setUsername(String userName1) {
    this.username = userName1;
  }

  /**
   * lower user name is also the login id (make sure lower)
   */
  @GcPersistableField(columnName="LOWER_USER_NAME")
  private String lowerUsername;

  /**
   * lower user name is also the login id (make sure lower)
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#getLowerUsername()
   */
  public String getLowerUsername() {
    return this.lowerUsername;
  }


  /**
   * lower user name is also the login id (make sure lower)
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianUserMapping#setLowerUsername(java.lang.String)
   */
  public void setLowerUsername(String lowerUserName1) {
    this.lowerUsername = lowerUserName1;
  }
  
}

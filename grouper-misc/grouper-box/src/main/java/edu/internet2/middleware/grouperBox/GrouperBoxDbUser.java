package edu.internet2.middleware.grouperBox;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

@GcPersistableClass(tableName="penn_box_user", defaultFieldPersist=GcPersist.doPersist)
public class GrouperBoxDbUser implements GcSqlAssignPrimaryKey {

  /**
   * 
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.boxId != null) {
      return false;
    }
    this.boxId = this.boxIdForInsert;
    return true;
  }

  public GrouperBoxDbUser() {
    
  }

  public List<GrouperBoxDbUser> retrieveAllFromDatabase() {
    List<GrouperBoxDbUser> grouperBoxDbUserList = new GcDbAccess()
        .sql("select * from penn_box_user").selectList(GrouperBoxDbUser.class);
    
    return grouperBoxDbUserList;

  }
 
  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof GrouperBoxDbUser)) {
      return false;
      
    }
    
    GrouperBoxDbUser other = (GrouperBoxDbUser) obj;
    return new EqualsBuilder()
        .append(this.boxId, other.boxId)
        // ignore boxIdForInsert
        .append(this.createdAt, other.createdAt)
        .append(this.login, other.login)
        .append(this.modifiedAt, other.modifiedAt)
        .append(this.name, other.name)
        .append(this.spaceUsed, other.spaceUsed)
        .append(this.status, other.status)
        .append(this.subjectId, other.subjectId)
        .isEquals();
  }

  /**
   * Computes the hash code based on the origin and destination nodes
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
      return new HashCodeBuilder()
        .append( this.boxId)
        // ignore boxIdForInsert
        .append( this.createdAt)
        .append( this.login)
        .append( this.modifiedAt)
        .append( this.name)
        .append( this.spaceUsed)
        .append( this.status)
        .append( this.subjectId)
        .toHashCode();
  }

  /**
   * 
   */
  private void storePrepare() {
    
    this.name = GrouperClientUtils.abbreviate(this.name, 100);
  }

  /**
   * 
   */
  public void store() {
    
    this.storePrepare();
    new GcDbAccess().storeToDatabase(this);

  }
  
  /**
   * delete batch
   * @param gcGrouperSyncGroups
   * @return rows deleted (groups and logs)
   */
  public static int deleteBatch(Collection<GrouperBoxDbUser> grouperBoxDbUsers) {
    int count = 0;
  
    if (GrouperClientUtils.length(grouperBoxDbUsers) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    for (GrouperBoxDbUser grouperBoxDbUser : grouperBoxDbUsers) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(grouperBoxDbUser.getBoxId());
      batchBindVars.add(currentBindVarRow);
      
    }
  
    int[] rowDeleteCounts = new GcDbAccess().sql("delete from penn_box_user where box_id = ?")
      .batchBindVars(batchBindVars).batchSize(200).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }
  
  /**
   * store batch of inserts or updates but not both
   * @param grouperBoxDbUsers
   * @return number of changes
   */
  public static int storeBatch(Collection<GrouperBoxDbUser> grouperBoxDbUsers) {
  
    if (GrouperClientUtils.length(grouperBoxDbUsers) == 0) {
      return 0;
    }
  
    int batchSize = 200;
  
    List<GrouperBoxDbUser> grouperBoxDbUsersList = new ArrayList<GrouperBoxDbUser>(grouperBoxDbUsers);
    
    for (GrouperBoxDbUser grouperBoxDbUser : GrouperClientUtils.nonNull(grouperBoxDbUsersList)) {
      grouperBoxDbUser.storePrepare();
    }
  
    int changes = new GcDbAccess().storeBatchToDatabase(grouperBoxDbUsersList, batchSize);
    
    return changes;
  }
  
  /**
   * max 100
   */
  private String subjectId;

  /**
   * 
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String boxId;

  /**
   * trick to tell difference between inserts and updates
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private String boxIdForInsert;

  /**
   * trick to tell difference between inserts and updates
   * @return
   */
  public String getBoxIdForInsert() {
    return boxIdForInsert;
  }

  /**
   * trick to tell difference between inserts and updates
   * @param boxIdForInsert
   */
  public void setBoxIdForInsert(String boxIdForInsert) {
    this.boxIdForInsert = boxIdForInsert;
  }

  /**
   * timestamp in seconds
   */
  private Timestamp createdAt;

  /**
   * eppn max 100
   */
  private String login;
  
  /**
   * modified at
   */
  private Timestamp modifiedAt;

  /**
   * name first last max 100
   */
  private String name;

  /**
   * space used in bytes
   */
  private Long spaceUsed;

  /**
   * status enum max 40: ACTIVE, CANNOT_DELETE_EDIT, CANNOT_DELETE_EDIT_UPLOAD, INACTIVE
   */
  private String status;

  
  public String getSubjectId() {
    return subjectId;
  }

  
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  
  public String getBoxId() {
    return boxId;
  }

  
  public void setBoxId(String boxId) {
    this.boxId = boxId;
  }

  
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  
  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  
  public String getLogin() {
    return login;
  }

  
  public void setLogin(String login) {
    this.login = login;
  }

  
  public Timestamp getModifiedAt() {
    return modifiedAt;
  }

  
  public void setModifiedAt(Timestamp modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public Long getSpaceUsed() {
    return spaceUsed;
  }

  
  public void setSpaceUsed(Long spaceUsed) {
    this.spaceUsed = spaceUsed;
  }

  
  public String getStatus() {
    return status;
  }

  
  public void setStatus(String status) {
    this.status = status;
  }

  
}

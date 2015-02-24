/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v2;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
@GcPersistableClass(tableName="CWD_MEMBERSHIP", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdMembershipV2 implements AtlassianCwdMembership {

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
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1) {
    this.id = id1;
  }

  /**
   * directory_id col
   * @param directoryId1 the directoryId to set
   */
  public void setDirectoryId(Long directoryId1) {
  }

  /**
   * parent id
   */
  @GcPersistableField
  private Long parentId;
  
  /**
   * parent id
   * @return the parentId
   */
  public Long getParentId() {
    return this.parentId;
  }
  
  /**
   * parent id
   * @param parentId1 the parentId to set
   */
  public void setParentId(Long parentId1) {
    this.parentId = parentId1;
  }

  /**
   * child_id col
   */
  @GcPersistableField
  private Long childGroupId;
  
  /**
   * child_id col
   */
  @GcPersistableField
  private Long childUserId;

  /**
   * @param parentName1 the parentName to set
   */
  public void setParentName(String parentName1) {
  }

  /**
   * lower_parent_name col
   * @param lowerParentName1 the lowerParentName to set
   */
  public void setLowerParentName(String lowerParentName1) {
  }

  /**
   * child_name col
   * @param childName1 the childName to set
   */
  public void setChildName(String childName1) {
  }
  
  /**
   * lower_child_name col
   * @param lowerChildName1 the lowerChildName to set
   */
  public void setLowerChildName(String lowerChildName1) {
  }


  /**
   * child_id col
   * @param childId1 the childId to set
   */
  public void setChildId(Long childId1) {
    this.childUserId = childId1;
  }

  /**
   * child_id col
   * @return child id
   */
  public Long getChildId() {
    return this.childUserId;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdMembership [id=" + this.id + ", parentId="
        + this.parentId 
        + ", childGroupId=" + this.childGroupId 
        + ", childUserId=" + this.childUserId + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all memberships
   * @return the memberships
   */
  public static List<AtlassianCwdMembership> retrieveMemberships() {
    
    List<AtlassianCwdMembershipV2> resultList = new GcDbAccess()
      .sql("select * from cwd_membership where child_user_id is not null")
      .selectList(AtlassianCwdMembershipV2.class);
    
    List<AtlassianCwdMembership> result = new ArrayList<AtlassianCwdMembership>();
    result.addAll(resultList);
    return result;
  }

  /**
   * store this record insert or update
   */
  public void store() {
    try {
      new GcDbAccess().storeToDatabase(this);
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, this.toString());
      throw re;
    }
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
    Long maxId = new GcDbAccess().sql("select max(id) from cwd_membership").select(Long.class);
    this.setId(maxId + 1);
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getParentName()
   */
  public String getParentName() {
    return this.parentId == null ? null : this.parentId.toString();
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getChildName()
   */
  public String getChildName() {
    return this.childUserId == null ? null : this.childUserId.toString();
  }

  
}

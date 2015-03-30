/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v4;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;


/**
 *
 */
@GcPersistableClass(tableName="membershipbase", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdMembershipV4 implements AtlassianCwdMembership {

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
   * parent id
   * @return the parentId
   */
  public Long getParentId() {
    return null;
  }
  
  /**
   * parent id
   * @param parentId1 the parentId to set
   */
  public void setParentId(Long parentId1) {
  }

  /**
   * child_id col
   * @return the childId
   */
  public Long getChildId() {
    return null;
  }

  /**
   * parent_name col
   */
  @GcPersistableField(columnName="group_name")
  private String parentName;

  /**
   * @return the parentName
   */
  public String getParentName() {
    return this.parentName;
  }

  /**
   * @param parentName1 the parentName to set
   */
  public void setParentName(String parentName1) {
    this.parentName = parentName1;
  }

  /**
   * lower_parent_name col
   * @param lowerParentName1 the lowerParentName to set
   */
  public void setLowerParentName(String lowerParentName1) {
  }

  /**
   * child_name col
   */
  @GcPersistableField(columnName="user_name")
  private String childName;
  
  /**
   * child_name col
   * @return the childName
   */
  public String getChildName() {
    return this.childName;
  }
  
  /**
   * child_name col
   * @param childName1 the childName to set
   */
  public void setChildName(String childName1) {
    this.childName = childName1;
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
  }



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdMembershipV4 [id=" + this.id + ", directoryId=" 
        + ", parentName=" + this.parentName + ", childName="
        + this.childName + "]";
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

    List<AtlassianCwdMembershipV4> resultList = new GcDbAccess().selectList(AtlassianCwdMembershipV4.class);
    List<AtlassianCwdMembership> result = new ArrayList<AtlassianCwdMembership>();
    result.addAll(resultList);
    return result;
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
    Long maxId = new GcDbAccess().sql("select max(id) from membershipbase").select(Long.class);
    if (maxId == null) {
      maxId = 20000L;
    }
    this.setId(maxId + 1);
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }
 
}

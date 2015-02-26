/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v0;

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
@GcPersistableClass(tableName="local_members", defaultFieldPersist=GcPersist.persistIfPersistableField, hasNoPrimaryKey=true)
public class AtlassianCwdMembershipV0 implements AtlassianCwdMembership {

  /**
   * parent id
   */
  @GcPersistableField(columnName="groupid")
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
  @GcPersistableField(columnName="userid")
  private Long childId;
  
  /**
   * child_id col
   * @return the childId
   */
  public Long getChildId() {
    return this.childId;
  }

  /**
   * child_id col
   * @param childId1 the childId to set
   */
  public void setChildId(Long childId1) {
    this.childId = childId1;
  }



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdMembership0 [parentId=" + this.parentId + ", childId=" + this.childId + "]";
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
    
    List<AtlassianCwdMembershipV0> resultList = new GcDbAccess()
      .sql("select * from local_members")
      .selectList(AtlassianCwdMembershipV0.class);
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
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getId()
   */
  public Long getId() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setId(java.lang.Long)
   */
  public void setId(Long id1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getParentName()
   */
  public String getParentName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setParentName(java.lang.String)
   */
  public void setParentName(String parentName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setLowerParentName(java.lang.String)
   */
  public void setLowerParentName(String lowerParentName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getChildName()
   */
  public String getChildName() {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setChildName(java.lang.String)
   */
  public void setChildName(String childName1) {
  }

  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#setLowerChildName(java.lang.String)
   */
  public void setLowerChildName(String lowerChildName1) {
  }
  
}

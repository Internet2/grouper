/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v3;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership;
import edu.internet2.middleware.grouperAtlassianConnector.db.GrouperAtlassianDataReconcile;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
@GcPersistableClass(tableName="CWD_MEMBERSHIP", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdMembershipV3 implements AtlassianCwdMembership {

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
   * parent id col as string
   * @return the id
   */
  public String getParentIdString() {
    return GrouperClientUtils.stringValue(this.parentId);
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
  private Long childId;
  
  /**
   * child_id col
   * @return the childId
   */
  public Long getChildId() {
    return this.childId;
  }

  /**
   * membership_type col
   */
  @GcPersistableField
  private String membershipType;

  /**
   * membership_type col
   * @return the membershipType
   */
  public String getMembershipType() {
    return this.membershipType;
  }
  
  /**
   * membership_type col
   * @param membershipType1 the membershipType to set
   */
  public void setMembershipType(String membershipType1) {
    this.membershipType = membershipType1;
  }

  /**
   * parent_name col
   */
  @GcPersistableField
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
   */
  @GcPersistableField
  private String lowerParentName;
  
  /**
   * lower_parent_name col
   * @return the lowerParentName
   */
  public String getLowerParentName() {
    return this.lowerParentName;
  }
  
  /**
   * lower_parent_name col
   * @param lowerParentName1 the lowerParentName to set
   */
  public void setLowerParentName(String lowerParentName1) {
    this.lowerParentName = lowerParentName1;
  }

  /**
   * child_name col
   */
  @GcPersistableField
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
   */
  @GcPersistableField
  private String lowerChildName;

  /**
   * lower_child_name col
   * @return the lowerChildName
   */
  public String getLowerChildName() {
    return this.lowerChildName;
  }
  
  /**
   * lower_child_name col
   * @param lowerChildName1 the lowerChildName to set
   */
  public void setLowerChildName(String lowerChildName1) {
    this.lowerChildName = lowerChildName1;
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
    return "AtlassianCwdMembership [id=" + this.id + ", directoryId=" + this.directoryId + ", parentId="
        + this.parentId + ", childId=" + this.childId + ", membershipType=" + this.membershipType
        + ", parentName=" + this.parentName + ", lowerParentName=" + this.lowerParentName + ", childName="
        + this.childName + ", lowerChildName=" + this.lowerChildName + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

    this.setMembershipType("GROUP_USER");
    this.setDirectoryId(GrouperAtlassianDataReconcile.directoryId());

  }
  
  /**
   * get all memberships
   * @return the memberships
   */
  public static List<AtlassianCwdMembership> retrieveMemberships() {
    
    List<AtlassianCwdMembershipV3> resultList = new GcDbAccess()
      .sql("select * from cwd_membership where membership_type = 'GROUP_USER'")
      .selectList(AtlassianCwdMembershipV3.class);
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
    Long maxId = new GcDbAccess().sql("select max(id) from cwd_membership").select(Long.class);
    this.setId(maxId + 1);
  }
  
}

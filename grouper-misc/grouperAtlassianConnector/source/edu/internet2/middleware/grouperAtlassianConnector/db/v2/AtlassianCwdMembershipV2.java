/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
   * parent name
   */
  private String parentName;

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
   * child name
   */
  private String childName;
  
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
    this.childUserId = childId1;
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


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getParentName()
   */
  public String getParentName() {
    return this.parentName;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdMembership#getChildName()
   */
  public String getChildName() {
    return this.childName;
  }

  
}

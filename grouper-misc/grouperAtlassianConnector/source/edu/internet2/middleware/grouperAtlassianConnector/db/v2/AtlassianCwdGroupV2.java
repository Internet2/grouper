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

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup;
import edu.internet2.middleware.grouperAtlassianConnector.db.GrouperAtlassianDataReconcile;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;


/**
 *
 */
@GcPersistableClass(tableName="CWD_GROUP", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdGroupV2 implements AtlassianCwdGroup {

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
   * group_name col
   */
  @GcPersistableField
  private String groupName;
  
  /**
   * group_name col
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  
  /**
   * group_name col
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * active col
   */
  @GcPersistableField
  private String active;

  /**
   * active col
   * @return the active
   */
  public String getActive() {
    return this.active;
  }

  
  /**
   * active col
   * @param active1 the active to set
   */
  public void setActive(String active1) {
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
   * lower_group_name col
   */
  @GcPersistableField
  private String lowerGroupName;

  
  
  /**
   * lower_group_name col
   * @return the lowerGroupName
   */
  public String getLowerGroupName() {
    return this.lowerGroupName;
  }

  
  /**
   * lower_group_name col
   * @param lowerGroupName1 the lowerGroupName to set
   */
  public void setLowerGroupName(String lowerGroupName1) {
    this.lowerGroupName = lowerGroupName1;
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
   * lower_description col
   * @param lowerDescription1 the lowerDescription to set
   */
  public void setLowerDescription(String lowerDescription1) {
    //dont do anything
  }

  /**
   * description col
   */
  @GcPersistableField
  private String description;

  
  /**
   * description col
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * description col
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /** group_type col */
  @GcPersistableField
  private String groupType;
  
  /**
   * group_type col
   * @return the grouptype
   */
  public String getGroupType() {
    return this.groupType;
  }
  
  /**
   * group_type col
   * @param groupType1 the grouptype to set
   */
  public void setGroupType(String groupType1) {
    this.groupType = groupType1;
  }

  /**
   * local col
   */
  @GcPersistableField
  private String local;
  
  /**
   * local col
   * @return the local
   */
  public String getLocal() {
    return this.local;
  }
  
  /**
   * local col
   * @param local1 the local to set
   */
  public void setLocal(String local1) {
    this.local = local1;
  }



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdGroupV2 [id=" + this.id + ", groupName=" + this.groupName + ", active=" + this.active
        + ", updatedDate=" + this.updatedDate + ", directoryId=" + this.directoryId + ", lowerGroupName="
        + this.lowerGroupName + ", createdDate=" + this.createdDate + ", description=" 
        + this.description + ", groupType=" + this.groupType
        + ", local=" + this.local + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

    this.setLocal("F");
    this.setGroupType("GROUP");
    this.setActive("T");
    Timestamp now = new Timestamp(System.currentTimeMillis());
    this.setCreatedDate(now);
    this.setUpdatedDate(now);
    
    this.setDirectoryId(GrouperAtlassianDataReconcile.directoryId());

  }
  
  /**
   * get all groups
   * @return the groups or null by map of groupname to group
   */
  public static Map<String, AtlassianCwdGroup> retrieveGroups() {
    
    List<AtlassianCwdGroupV2> resultList = new GcDbAccess().selectList(AtlassianCwdGroupV2.class);
    Map<String, AtlassianCwdGroup> resultMap = new LinkedHashMap<String, AtlassianCwdGroup>();
    for (AtlassianCwdGroupV2 atlassianCwdGroup : resultList) {
      resultMap.put(atlassianCwdGroup.getGroupName(), atlassianCwdGroup);
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
    Long maxId = new GcDbAccess().sql("select max(id) from cwd_group").select(Long.class);
    this.setId(maxId + 1);
  }
  

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.groupName == null) ? 0 : this.groupName.hashCode());
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
    AtlassianCwdGroupV2 other = (AtlassianCwdGroupV2) obj;
    if (this.groupName == null) {
      if (other.groupName != null)
        return false;
    } else if (!this.groupName.equals(other.groupName))
      return false;
    return true;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return this.active == null ? null : this.active.equals("T");
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
    this.active = active1 == null ? null : (active1 ? "T" : "F");
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getLocalBoolean()
   */
  public Boolean getLocalBoolean() {
    return this.local == null ? null : this.local.equals("T");
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLocalBoolean(java.lang.Boolean)
   */
  public void setLocalBoolean(Boolean local1) {
    this.local = local1 == null ? null : (local1 ? "T" : "F");
  }
  
}

/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db.v0;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
@GcPersistableClass(tableName="groups", defaultFieldPersist=GcPersist.persistIfPersistableField)
public class AtlassianCwdGroupV0 implements AtlassianCwdGroup {

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
   * @return the id
   */
  public String getIdString() {
    return GrouperClientUtils.stringValue(this.id);
  }
  
  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getActiveBoolean()
   */
  public Boolean getActiveBoolean() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setActiveBoolean(java.lang.Boolean)
   */
  public void setActiveBoolean(Boolean active1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getUpdatedDate()
   */
  public Timestamp getUpdatedDate() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setUpdatedDate(java.sql.Timestamp)
   */
  public void setUpdatedDate(Timestamp updatedDate1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getDirectoryId()
   */
  public Long getDirectoryId() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setDirectoryId(java.lang.Long)
   */
  public void setDirectoryId(Long directoryId1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getLowerGroupName()
   */
  public String getLowerGroupName() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLowerGroupName(java.lang.String)
   */
  public void setLowerGroupName(String lowerGroupName1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getCreatedDate()
   */
  public Timestamp getCreatedDate() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setCreatedDate(java.sql.Timestamp)
   */
  public void setCreatedDate(Timestamp createdDate1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLowerDescription(java.lang.String)
   */
  public void setLowerDescription(String lowerDescription1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getDescription()
   */
  public String getDescription() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setDescription(java.lang.String)
   */
  public void setDescription(String description1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getGroupType()
   */
  public String getGroupType() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setGroupType(java.lang.String)
   */
  public void setGroupType(String groupType1) {
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#getLocalBoolean()
   */
  public Boolean getLocalBoolean() {
    return null;
  }


  /**
   * @see edu.internet2.middleware.grouperAtlassianConnector.db.AtlassianCwdGroup#setLocalBoolean(java.lang.Boolean)
   */
  public void setLocalBoolean(Boolean local1) {
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
  @GcPersistableField(columnName="groupname")
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AtlassianCwdGroupV0 [id=" + this.id + ", groupName=" + this.groupName + "]";
  }


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject() {

  }
  
  /**
   * get all groups
   * @return the groups or null by map of groupname to group
   */
  public static Map<String, AtlassianCwdGroup> retrieveGroups() {
    
    List<AtlassianCwdGroupV0> resultList = new GcDbAccess().selectList(AtlassianCwdGroupV0.class);
    Map<String, AtlassianCwdGroup> resultMap = new LinkedHashMap<String, AtlassianCwdGroup>();
    for (AtlassianCwdGroupV0 atlassianCwdGroup : resultList) {
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
    Long maxId = new GcDbAccess().sql("select max(id) from groups").select(Long.class);
    this.setId(maxId == null ? 0 : maxId + 1);
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
    AtlassianCwdGroupV0 other = (AtlassianCwdGroupV0) obj;
    if (this.groupName == null) {
      if (other.groupName != null)
        return false;
    } else if (!this.groupName.equals(other.groupName))
      return false;
    return true;
  }
  
}

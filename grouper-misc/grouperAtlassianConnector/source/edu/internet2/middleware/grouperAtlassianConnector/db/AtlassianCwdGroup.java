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
package edu.internet2.middleware.grouperAtlassianConnector.db;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;


/**
 *
 */
public interface AtlassianCwdGroup extends GcSqlAssignPrimaryKey {

  /**
   * id col
   * @return the id
   */
  public Long getId();

  /**
   * id col
   * @return the id
   */
  public String getIdString();
  
  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1);

  /**
   * group_name col
   * @return the groupName
   */
  public String getGroupName();

  
  /**
   * group_name col
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1);

  /**
   * active col
   * @return the active
   */
  public Boolean getActiveBoolean();

  
  /**
   * active col
   * @param active1 the active to set
   */
  public void setActiveBoolean(Boolean active1);
  
  /**
   * updated_date col
   * @return the updatedDate
   */
  public Timestamp getUpdatedDate();

  
  /**
   * updated_date col
   * @param updatedDate1 the updatedDate to set
   */
  public void setUpdatedDate(Timestamp updatedDate1);

  /**
   * directory_id col
   * @return the directoryId
   */
  public Long getDirectoryId();
  
  /**
   * directory_id col
   * @param directoryId1 the directoryId to set
   */
  public void setDirectoryId(Long directoryId1);

  /**
   * lower_group_name col
   * @return the lowerGroupName
   */
  public String getLowerGroupName();

  
  /**
   * lower_group_name col
   * @param lowerGroupName1 the lowerGroupName to set
   */
  public void setLowerGroupName(String lowerGroupName1);

  /**
   * created date col
   * @return the createdDate
   */
  public Timestamp getCreatedDate();

  
  /**
   * created date col
   * @param createdDate1 the createdDate to set
   */
  public void setCreatedDate(Timestamp createdDate1);

  /**
   * lower_description col
   * @param lowerDescription1 the lowerDescription to set
   */
  public void setLowerDescription(String lowerDescription1);

  /**
   * description col
   * @return the description
   */
  public String getDescription();

  
  /**
   * description col
   * @param description1 the description to set
   */
  public void setDescription(String description1);

  /**
   * group_type col
   * @return the grouptype
   */
  public String getGroupType();
  
  /**
   * group_type col
   * @param groupType1 the grouptype to set
   */
  public void setGroupType(String groupType1);

  /**
   * local col
   * @return the local
   */
  public Boolean getLocalBoolean();
  
  /**
   * local col
   * @param local1 the local to set
   */
  public void setLocalBoolean(Boolean local1);



  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public abstract String toString();


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject();  

  /**
   * store this record insert or update
   */
  public void store();

  /**
   * delete this record
   */
  public void delete();

  

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode();

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj);
  
}

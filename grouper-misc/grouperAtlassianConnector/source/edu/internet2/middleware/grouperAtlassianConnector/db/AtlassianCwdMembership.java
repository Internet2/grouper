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

import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;


/**
 *
 */
public interface AtlassianCwdMembership extends GcSqlAssignPrimaryKey {

  /**
   * id col
   * @return the id
   */
  public Long getId();

  
  /**
   * id col
   * @param id1 the id to set
   */
  public void setId(Long id1);
  
  /**
   * directory_id col
   * @param directoryId1 the directoryId to set
   */
  public void setDirectoryId(Long directoryId1);
  
  /**
   * parent id
   * @return the parentId
   */
  public Long getParentId();
  
  /**
   * parent id
   * @param parentId1 the parentId to set
   */
  public void setParentId(Long parentId1);

  /**
   * @return the parentName
   */
  public String getParentName();

  /**
   * @param parentName1 the parentName to set
   */
  public void setParentName(String parentName1);

  /**
   * lower_parent_name col
   * @param lowerParentName1 the lowerParentName to set
   */
  public void setLowerParentName(String lowerParentName1);

  /**
   * child_name col
   * @return the childName
   */
  public String getChildName();
  
  /**
   * child_name col
   * @param childName1 the childName to set
   */
  public void setChildName(String childName1);

  /**
   * lower_child_name col
   * @param lowerChildName1 the lowerChildName to set
   */
  public void setLowerChildName(String lowerChildName1);

  /**
   * child_id col
   * @param childId1 the childId to set
   */
  public void setChildId(Long childId1);

  /**
   * child_id col
   * @return the child id
   */
  public Long getChildId();

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString();

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
  
}

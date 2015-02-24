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

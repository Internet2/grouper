/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.tree;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * one tree item with a list of chilren if applicable
 * 
 * {
 *  "name": "US Government",
 *  "id": "root",
 *  children: true,
 * 
 * @author mchyzer
 *
 */
public class DojoTreeItemChild {

  /**
   * type of child element
   *
   */
  public static enum DojoTreeItemType {
    
    /** if group */
    group,
    
    /** if stem */
    stem;
  }
  
  /**
   * easy constructor
   * @param name
   * @param id
   * @param hasChildren
   * @param children
   */
  public DojoTreeItemChild(String name, String id, 
      DojoTreeItemType dojoTreeItemChildType, Boolean hasChildren) {
    super();
    this.name = name;
    this.id = id;
    this.children = hasChildren;
    this.assignTheTypeEnum(dojoTreeItemChildType);
  }

  /** if group or folder */
  private String theType = null;
  
  
  
  /**
   * if group or folder 
   * @return if group or folder
   */
  public String getTheType() {
    return theType;
  }

  /**
   * if group or folder
   * @param theType
   */
  public void setTheType(String theType) {
    this.theType = theType;
  }

  /**
   * assign the type of child
   * @param dojoTreeItemChildType1
   */
  public void assignTheTypeEnum(DojoTreeItemType dojoTreeItemChildType1) {
    this.theType = dojoTreeItemChildType1 == null ? null : dojoTreeItemChildType1.name();
  }
  
  /**
   * convert this object to json
   * @return the json
   */
  public String toJson() {
    return GrouperUtil.jsonConvertTo(this, false);
  }
  
  /**
   * 
   */
  public DojoTreeItemChild() {
  }

  /** what displays on the screen, display extension */
  private String name;
  
  /** id to get the children of this node in URL */
  private String id;

  /** if this node has children or not */
  private Boolean children;
  
  /**
   * what displays on the screen, display extension
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * what displays on the screen, display extension
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * id to get the children of this node in URL
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id to get the children of this node in URL
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * if this node has children or not
   * @return if this node has children or not
   */
  public Boolean getChildren() {
    return this.children;
  }

  /**
   * if this node has children or not
   * @param hasChildren1
   */
  public void setChildren(Boolean hasChildren1) {
    this.children = hasChildren1;
  }
  
  
}

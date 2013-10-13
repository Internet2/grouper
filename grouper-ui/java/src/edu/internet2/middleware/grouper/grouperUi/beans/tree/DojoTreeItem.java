/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.tree;

import edu.internet2.middleware.grouper.grouperUi.beans.tree.DojoTreeItemChild.DojoTreeItemChildType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * 
 * one tree item with a list of children if applicable
 * 
 * {
 *  "name": "US Government",
 *  "id": "root",
 *  "children": [
 * 
 * @author mchyzer
 *
 */
public class DojoTreeItem {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    DojoTreeItem root = new DojoTreeItem("Root", "root");
    DojoTreeItemChild child1 = new DojoTreeItemChild("Child1", "child1", DojoTreeItemChildType.group, true);
    DojoTreeItemChild child2 = new DojoTreeItemChild("Child2", "child2", DojoTreeItemChildType.stem, null);
    root.setChildren(new DojoTreeItemChild[]{child1, child2});
    System.out.println(root.toJson());
    //{"children":[{"children":true,"id":"child1","name":"Child1"},{"id":"child2","name":"Child2"}],"id":"root","name":"Root"}

  }

  /**
   * easy constructor
   * @param name
   * @param id
   * @param hasChildren
   * @param children
   */
  public DojoTreeItem(String name, String id) {
    super();
    this.name = name;
    this.id = id;
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
  public DojoTreeItem() {
  }

  /** what displays on the screen, display extension */
  private String name;
  
  /** id to get the children of this node in URL */
  private String id;

  /** children of this node */
  private DojoTreeItemChild[] children;

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
   * children of this node
   * @return children of this node
   */
  public DojoTreeItemChild[] getChildren() {
    return this.children;
  }

  /**
   * children of this node
   * @param children1
   */
  public void setChildren(DojoTreeItemChild[] children1) {
    this.children = children1;
  }

  
  
}

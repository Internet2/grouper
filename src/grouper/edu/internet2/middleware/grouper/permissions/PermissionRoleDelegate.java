/**
 * @author mchyzer
 * $Id: PermissionRoleDelegate.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.io.Serializable;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * delegate the role
 */
@SuppressWarnings("serial")
public class PermissionRoleDelegate implements Serializable {

  /** keep a reference to the group */
  private Group group;
  
  /**
   * 
   * @param group1
   */
  public PermissionRoleDelegate(Group group1) {
    this.group = group1;
    assertIsRole(group1);

  }

  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * @param attributeDefName
   */
  public void assignRolePermission(AttributeDefName attributeDefName) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    this.group.getAttributeDelegate().assignAttribute(attributeDefName);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   */
  public void assignSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    this.group.getAttributeDelegateEffMship(member).assignAttribute(attributeDefName);
  }
  
  /**
   * assert that this is a role
   * @param object 
   */
  private static void assertIsRole(Object object) {
    if (!(object instanceof Group)) {
      throw new RuntimeException("Expecting Group object, was: " + GrouperUtil.className(object));
    }
    Group group = (Group)object;
    if (!TypeOfGroup.role.equals(group.getTypeOfGroup())) {
      throw new RuntimeException("Requires this group to be of type 'role', but" +
          " instead is of type: " + group.getTypeOfGroup() + ": " + group.getName());
    }
  } 

  
}

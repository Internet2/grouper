/**
 * @author mchyzer
 * $Id: PermissionRoleDelegate.java,v 1.3 2009-10-05 00:50:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.io.Serializable;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


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
    this.assignRolePermission(null, attributeDefName);
  }
  
  /**
   * remove a permission from a role, which means that any subject in the role will not directly have this permission, 
   * and any role in the roleSet directed graph will also not get this permission from this role
   * @param attributeDefName
   */
  public void removeRolePermission(AttributeDefName attributeDefName) {
    removeRolePermission(null, attributeDefName);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   */
  public void assignSubjectRolePermission(AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    assignSubjectRolePermission(null, attributeDefName, member);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   */
  public void removeSubjectRolePermission(AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

    removeSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   */
  public void assignRolePermission(String action, AttributeDefName attributeDefName) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    this.group.getAttributeDelegate().assignAttribute(action, attributeDefName);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   */
  public void assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    assignSubjectRolePermission(action, attributeDefName, member);
  }

  /**
   * remove a permission from a role, which means that any subject in the role will not directly have this permission, 
   * and any role in the roleSet directed graph will also not get this permission from this role
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   */
  public void removeRolePermission(String action, AttributeDefName attributeDefName) {
    this.group.getAttributeDelegate().removeAttribute(action, attributeDefName);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   */
  public void removeSubjectRolePermission(String action, AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    removeSubjectRolePermission(action, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   */
  public void assignSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    assignSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   */
  public void assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Member member) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    this.group.getAttributeDelegateEffMship(member).assignAttribute(action, attributeDefName);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   */
  public void removeSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    removeSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   */
  public void removeSubjectRolePermission(String action, AttributeDefName attributeDefName, Member member) {
    this.group.getAttributeDelegateEffMship(member).removeAttribute(action, attributeDefName);
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

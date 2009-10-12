/**
 * @author mchyzer
 * $Id: PermissionRoleDelegate.java,v 1.4 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.io.Serializable;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegateOptions;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
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
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignRolePermission(AttributeDefName attributeDefName) {
    return this.assignRolePermission(null, attributeDefName);
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
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return assignSubjectRolePermission(null, attributeDefName, member);
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
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignRolePermission(String action, AttributeDefName attributeDefName) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegate().assignAttribute(action, attributeDefName);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return assignSubjectRolePermission(action, attributeDefName, member);
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
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    return assignSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Member member) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegateEffMship(member).assignAttribute(action, attributeDefName);
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
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission.
   * Note: the subject assigning must have delegate or grant on permission
   * @param attributeDefName
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateRolePermission(AttributeDefName attributeDefName,
      boolean assign, AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    return this.delegateRolePermission(null, attributeDefName, assign, attributeAssignDelegateOptions);
  }

  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * Note: the subject assigning must have delegate or grant on permission
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateRolePermission(String action, AttributeDefName attributeDefName,
      boolean assign, AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only delegate a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegate().delegateAttribute(action, attributeDefName, 
        assign, attributeAssignDelegateOptions);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * Note: the subject assigning must have delegate or grant on permission
   * @param attributeDefName
   * @param member 
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateSubjectRolePermission(
      AttributeDefName attributeDefName, Member member, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    return delegateSubjectRolePermission(null, attributeDefName, member, assign, attributeAssignDelegateOptions);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateSubjectRolePermission(
      AttributeDefName attributeDefName, Subject subject, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return delegateSubjectRolePermission(null, attributeDefName, member, assign, attributeAssignDelegateOptions);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateSubjectRolePermission(String action, 
      AttributeDefName attributeDefName, Member member, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only delegate a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegateEffMship(member)
      .delegateAttribute(action, attributeDefName, assign, attributeAssignDelegateOptions);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   * @param assign true to assign, false to remove
   * @param attributeAssignDelegateOptions options in the assignment, null if none
   * @return if new, and the assignment
   */
  public AttributeAssignResult delegateSubjectRolePermission(String action, 
      AttributeDefName attributeDefName, Subject subject, boolean assign, 
      AttributeAssignDelegateOptions attributeAssignDelegateOptions) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return delegateSubjectRolePermission(action, attributeDefName, member, assign, 
        attributeAssignDelegateOptions);
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

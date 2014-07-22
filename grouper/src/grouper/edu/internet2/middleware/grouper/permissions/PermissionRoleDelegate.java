/**
 * Copyright 2014 Internet2
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
 */
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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
   * @deprecated use assignRolePermission(attributeDefName, permissionAllowed) instead, will be removed some time after 2.0
   */
  @Deprecated
  public AttributeAssignResult assignRolePermission(AttributeDefName attributeDefName) {
    return this.assignRolePermission(attributeDefName, PermissionAllowed.ALLOWED);
  }
  
  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * @param attributeDefName
   * @param permissionAllowed
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignRolePermission(AttributeDefName attributeDefName, PermissionAllowed permissionAllowed) {
    return this.assignRolePermission(null, attributeDefName, permissionAllowed);
  }
  
  /**
   * remove a permission from a role, which means that any subject in the role will not directly have this permission, 
   * and any role in the roleSet directed graph will also not get this permission from this role
   * @param attributeDefName
   * @return result
   */
  public AttributeAssignResult removeRolePermission(AttributeDefName attributeDefName) {
    return removeRolePermission(null, attributeDefName);
  }
  
  /**
   * @param member
   * @param action
   * @param attributeDefName
   * @param checkSecurity
   * @param exceptionfNotFound
   * @return the assignment
   */
  public AttributeAssign retrieveAssignment(Member member, String action, AttributeDefName attributeDefName, boolean checkSecurity, boolean exceptionfNotFound) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only retrieve assignment of a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegateEffMship(member).retrieveAssignment(action, attributeDefName, checkSecurity, exceptionfNotFound);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   * @return if new, and the assignment
   * @deprecated use assignSubjectRolePermission(attributeDefName, subject, permissionAllowed) will be removed some time after 2.0
   */
  @Deprecated
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Subject subject) {
    return assignSubjectRolePermission(attributeDefName, subject, PermissionAllowed.ALLOWED);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   * @param permissionAllowed
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Subject subject, PermissionAllowed permissionAllowed) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return assignSubjectRolePermission(null, attributeDefName, member, permissionAllowed);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param subject 
   * @return result
   */
  public AttributeAssignResult removeSubjectRolePermission(AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

    return removeSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @return if new, and the assignment
   * @deprecated use assignRolePermission(action, attributeDefName, permissionAllowed), will remove this some time after 2.0
   */
  @Deprecated
  public AttributeAssignResult assignRolePermission(String action, AttributeDefName attributeDefName) {
    return assignRolePermission(action, attributeDefName, PermissionAllowed.ALLOWED);
  }

  /**
   * add a permission to a role, which means that any subject in the role will get this permission, 
   * and any role in the roleSet directed graph will also get the permission
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param permissionAllowed allowed or disallowed
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignRolePermission(String action, AttributeDefName attributeDefName, PermissionAllowed permissionAllowed) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Can only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegate().assignAttribute(action, attributeDefName, permissionAllowed);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   * @return if new, and the assignment
   * @deprecated use assignSubjectRolePermission(action, attributeDefName, subject, permissionAllowed) instead
   */
  @Deprecated
  public AttributeAssignResult assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Subject subject) {
    return assignSubjectRolePermission(action, attributeDefName, subject, PermissionAllowed.ALLOWED);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   * @param permissionAllowed 
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(String action, 
      AttributeDefName attributeDefName, Subject subject, PermissionAllowed permissionAllowed) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return assignSubjectRolePermission(action, attributeDefName, member, permissionAllowed);
  }

  /**
   * remove a permission from a role, which means that any subject in the role will not directly have this permission, 
   * and any role in the roleSet directed graph will also not get this permission from this role
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @return result
   */
  public AttributeAssignResult removeRolePermission(String action, AttributeDefName attributeDefName) {
    return this.group.getAttributeDelegate().removeAttribute(action, attributeDefName);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param subject 
   * @return result
   */
  public AttributeAssignResult removeSubjectRolePermission(String action, AttributeDefName attributeDefName, Subject subject) {
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return removeSubjectRolePermission(action, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   * @return if new, and the assignment
   * @deprecated use assignSubjectRolePermission(attributeDefName, member, permissionAllowed) will be removed some time after 2.0
   */
  @Deprecated
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    
    return assignSubjectRolePermission(attributeDefName, member, PermissionAllowed.ALLOWED);
    
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   * @param permissionAllowed 
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(AttributeDefName attributeDefName, Member member, PermissionAllowed permissionAllowed) {
    return assignSubjectRolePermission(null, attributeDefName, member, permissionAllowed);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   * @return if new, and the assignment
   * @deprecated use assignSubjectRolePermission(action, attributeDefName, member, permissionAllowed) will be removed some time after 2.0
   */
  @Deprecated
  public AttributeAssignResult assignSubjectRolePermission(String action, AttributeDefName attributeDefName, Member member) {
    return assignSubjectRolePermission(action, attributeDefName, member, PermissionAllowed.ALLOWED);
  }
  
  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   * @param permissionAllowed 
   * @return if new, and the assignment
   */
  public AttributeAssignResult assignSubjectRolePermission(String action, AttributeDefName attributeDefName, 
      Member member, PermissionAllowed permissionAllowed) {
    if (!AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType())) {
      throw new RuntimeException("Cant only assign a permission with attributeDefName as perm (permission) type: " 
          + attributeDefName.getName() + ", " + attributeDefName.getAttributeDef().getAttributeDefType());
    }
    return this.group.getAttributeDelegateEffMship(member).assignAttribute(action, attributeDefName, permissionAllowed);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param attributeDefName
   * @param member 
   * @return result
   */
  public AttributeAssignResult removeSubjectRolePermission(AttributeDefName attributeDefName, Member member) {
    return removeSubjectRolePermission(null, attributeDefName, member);
  }

  /**
   * add a permission to a role / subject pair (effective membership)
   * @param action is the action on the assignment (e.g. read, write, assign (default))
   * @param attributeDefName
   * @param member 
   * @return attribute assign result
   */
  public AttributeAssignResult removeSubjectRolePermission(String action, AttributeDefName attributeDefName, Member member) {
    return this.group.getAttributeDelegateEffMship(member).removeAttribute(action, attributeDefName);
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

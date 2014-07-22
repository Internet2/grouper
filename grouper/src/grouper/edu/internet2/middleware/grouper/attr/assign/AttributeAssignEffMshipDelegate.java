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
 * $Id: AttributeAssignEffMshipDelegate.java,v 1.3 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate attribute calls from effective memberships
 */
public class AttributeAssignEffMshipDelegate extends AttributeAssignBaseDelegate {

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assignAttribute(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public AttributeAssignResult assignAttribute(AttributeDefName attributeDefName) {
    //make sure the member is a member of the group before assigning
    assertMemberOfGroup();
    return super.assignAttribute(attributeDefName);
  }

  /**
   * reference to the group in question
   */
  private Group group = null;
  
  /**
   * reference to the group in question
   */
  private Member member = null;
  
  /** cache if member of group */
  private Boolean isMember = null;

  /**
   * make sure member is a member of the group
   */
  private void assertMemberOfGroup() {
    //see if we are ok
    if (this.isMember != null && this.isMember) {
        return;
    }
    if (this.isMember == null) {
      this.isMember = this.group.hasMember(this.member.getSubject());
    }
    if (!this.isMember) {
      throw new RuntimeException("Cant assign effective membership attribute if member " + GrouperUtil.subjectToString(this.member.getSubject()) 
          + " is not a member of group: " + this.group);
    }

  }
  
  /**
   * 
   * @param group1
   * @param member1
   */
  public AttributeAssignEffMshipDelegate(Group group1, Member member1) {
    this.group = group1;
    this.member = member1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#newAttributeAssign(java.lang.String, edu.internet2.middleware.grouper.attr.AttributeDefName, java.lang.String)
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName, String uuid) {
    assertMemberOfGroup();
    return new AttributeAssign(this.group, this.member, action, attributeDefName, uuid);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  public
  void assertCanReadAttributeDef(final AttributeDef attributeDef) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canReadGroup = new boolean[1];
  
    final boolean isPermission = AttributeDefType.perm.equals(attributeDef.getAttributeDefType());

    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        
        if (isPermission) {
          canReadGroup[0] = PrivilegeHelper.canGroupAttrRead(rootSession, AttributeAssignEffMshipDelegate.this.group, subject);
        } else {
          canReadGroup[0] = PrivilegeHelper.canRead(rootSession, AttributeAssignEffMshipDelegate.this.group, subject);
        }
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDef.getName());
    }
  
    if (!canReadGroup[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot view group " + this.group);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    //attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
    final boolean[] canUpdateGroup = new boolean[1];
    //this.group.hasAdmin(subject);
    
    final boolean isPermission = AttributeDefType.perm.equals(attributeDefName.getAttributeDef().getAttributeDefType());
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
        
        if (isPermission) {
          canUpdateGroup[0] = PrivilegeHelper.canGroupAttrUpdate(rootSession, AttributeAssignEffMshipDelegate.this.group, subject);
        } else {
          canUpdateGroup[0] = PrivilegeHelper.canUpdate(rootSession, AttributeAssignEffMshipDelegate.this.group, subject);
        }
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDef.getName());
    }

    if (!canUpdateGroup[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update group " + this.group);
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByGroupIdMemberIdAndAttributeDefNameId(this.group.getUuid(), this.member.getUuid(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) { 
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdMemberIdAndAttributeDefId(this.group.getUuid(), this.member.getUuid(), attributeDefId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByGroupIdMemberIdAndAttributeDefId(this.group.getUuid(), this.member.getUuid(), attributeDefId);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( "group", this.group)
      .append( "member", this.member )
      .toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#getAttributeAssignable()
   */
  @Override
  public AttributeAssignable getAttributeAssignable() {
    return new GroupMember(this.group, this.member);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwner()
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeAssignments(null, null, null, 
          GrouperUtil.toSet(new MultiKey(this.group.getId(), this.member.getUuid())), null, null, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwner()
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwner() {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAnyMembershipAttributeDefNames(null, null, null, GrouperUtil.toSet(new MultiKey(this.group.getId(), this.member.getUuid())),null, true);
  }

}

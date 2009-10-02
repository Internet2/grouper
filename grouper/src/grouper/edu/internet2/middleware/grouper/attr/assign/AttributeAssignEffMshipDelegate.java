/**
 * @author mchyzer
 * $Id: AttributeAssignEffMshipDelegate.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
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
  public boolean assignAttribute(AttributeDefName attributeDefName) {
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
      throw new RuntimeException("Cant create a delegate if member " + GrouperUtil.subjectToString(this.member.getSubject()) 
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
   * @param attributeDefName
   * @return attribute assign
   */
  @Override
  AttributeAssign newAttributeAssign(AttributeDefName attributeDefName) {
    assertMemberOfGroup();
    return new AttributeAssign(this.group, this.member, AttributeDef.ACTION_DEFAULT, attributeDefName);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  void assertCanReadAttributeDef(final AttributeDef attributeDef) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canReadGroup = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        canReadGroup[0] = PrivilegeHelper.canRead(rootSession, 
            AttributeAssignEffMshipDelegate.this.group, subject);
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
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    //attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
    final boolean[] canUpdateGroup = new boolean[1];
    //this.group.hasAdmin(subject);
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
        canUpdateGroup[0] = PrivilegeHelper.canUpdate(rootSession, AttributeAssignEffMshipDelegate.this.group, subject);
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

}

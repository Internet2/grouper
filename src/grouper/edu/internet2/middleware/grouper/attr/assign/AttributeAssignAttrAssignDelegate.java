/**
 * @author mchyzer
 * $Id: AttributeAssignAttrAssignDelegate.java,v 1.1 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
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
 * delegate privilege calls from attribute defs
 */
public class AttributeAssignAttrAssignDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the group in question
   */
  private AttributeAssign attributeAssignToAssignTo = null;
  
  /**
   * 
   * @param attributeAssign1
   */
  public AttributeAssignAttrAssignDelegate(AttributeAssign attributeAssign1) {
    this.attributeAssignToAssignTo = attributeAssign1;
  }
  
  /**
   * @param attributeDefName
   * @return attribute assign
   */
  @Override
  AttributeAssign newAttributeAssign(AttributeDefName attributeDefName) {
    return new AttributeAssign(this.attributeAssignToAssignTo, AttributeDef.ACTION_DEFAULT, attributeDefName);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  void assertCanReadAttributeDef(final AttributeDef attributeDef) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttributeAssigned = new boolean[1];
    final boolean[] canReadAttributeToAssignTo = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttributeAssigned[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        canReadAttributeToAssignTo[0] = AttributeAssignAttrAssignDelegate.this.attributeAssignToAssignTo.getAttributeDef()
          .getPrivilegeDelegate().canAttrRead(subject);
        return null;
      }
    });
    
    if (!canReadAttributeAssigned[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDef.getName());
    }
  
    if (!canReadAttributeToAssignTo[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + this.attributeAssignToAssignTo.getAttributeDef().getName());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefNameAssigned) {
    final AttributeDef attributeDefAssigned = attributeDefNameAssigned.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttributeAssigned = new boolean[1];
    final boolean[] canUpdateAttributeToAssign = new boolean[1];
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttributeAssigned[0] = attributeDefAssigned.getPrivilegeDelegate().canAttrUpdate(subject);
        canUpdateAttributeToAssign[0] = AttributeAssignAttrAssignDelegate.this.attributeAssignToAssignTo
          .getAttributeDef().getPrivilegeDelegate().canAttrUpdate(subject);
        return null;
      }
    });
    
    if (!canUpdateAttributeAssigned[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDefAssigned.getName());
    }

    if (!canUpdateAttributeToAssign[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + this.attributeAssignToAssignTo.getAttributeDef().getName());
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByAttrAssignIdAndAttributeDefNameId(this.attributeAssignToAssignTo.getId(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
    .getAttributeAssign().findByAttrAssignIdAndAttributeDefId(this.attributeAssignToAssignTo.getId(), attributeDefId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByAttrAssignIdAndAttributeDefId(this.attributeAssignToAssignTo.getId(), attributeDefId);
  }

}

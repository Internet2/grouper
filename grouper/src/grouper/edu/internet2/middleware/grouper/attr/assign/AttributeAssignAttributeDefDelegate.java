/**
 * @author mchyzer
 * $Id: AttributeAssignAttributeDefDelegate.java,v 1.2 2009-10-05 00:50:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

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
public class AttributeAssignAttributeDefDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the attributedef in question
   */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   * @param attributeDef1
   */
  public AttributeAssignAttributeDefDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }
  
  /**
   * @param attributeDefName
   * @return attribute assign
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName) {
    return new AttributeAssign(this.attributeDef, action, attributeDefName);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  void assertCanReadAttributeDef(final AttributeDef attributeDefToAssign) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canViewAttributeDefAssignTo = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDefToAssign.getPrivilegeDelegate().canAttrRead(subject);
        canViewAttributeDefAssignTo[0] = PrivilegeHelper.canAttrView(rootSession, AttributeAssignAttributeDefDelegate.this.attributeDef, subject);
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDefToAssign.getName());
    }
  
    if (!canViewAttributeDefAssignTo[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot view attributeDef " + AttributeAssignAttributeDefDelegate.this.attributeDef.getName());
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanUpdateAttributeDefName(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDefToAssign = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    final boolean[] canAdminAttributeDefToAssignTo = new boolean[1];
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDefToAssign.getPrivilegeDelegate().canAttrUpdate(subject);
        canAdminAttributeDefToAssignTo[0] = PrivilegeHelper.canAttrAdmin(rootSession, AttributeAssignAttributeDefDelegate.this.attributeDef, subject);
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDefToAssign.getName());
    }

    if (!canAdminAttributeDefToAssignTo[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot admin attributeDef " + AttributeAssignAttributeDefDelegate.this.attributeDef.getName());
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefNameId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(
      String attributeDefNameId) {
    return GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByAttributeDefIdAndAttributeDefNameId(this.attributeDef.getId(), attributeDefNameId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
    .getAttributeAssign().findByAttributeDefIdAndAttributeDefId(this.attributeDef.getUuid(), attributeDefId);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwnerAndAttributeDefId(java.lang.String)
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(
      String attributeDefId) {
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByAttributeDefIdAndAttributeDefId(this.attributeDef.getUuid(), attributeDefId);
  }

}

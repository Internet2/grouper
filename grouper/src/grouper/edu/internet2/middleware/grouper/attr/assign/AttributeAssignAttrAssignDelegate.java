/**
 * @author mchyzer
 * $Id: AttributeAssignAttrAssignDelegate.java,v 1.3 2009-10-12 09:46:34 mchyzer Exp $
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * delegate attribute calls from attribute assigns
 */
public class AttributeAssignAttrAssignDelegate extends AttributeAssignBaseDelegate {

  /**
   * reference to the assignment in question
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
   * 
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#newAttributeAssign(java.lang.String, edu.internet2.middleware.grouper.attr.AttributeDefName, java.lang.String)
   */
  @Override
  AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName, String uuid) {
    return new AttributeAssign(this.attributeAssignToAssignTo, action, attributeDefName, uuid);
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanReadAttributeDef(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  @Override
  public void assertCanReadAttributeDef(final AttributeDef attributeDef) {
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
  public
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

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#toString()
   */
  @Override
  public String toString() {
    return this.attributeAssignToAssignTo == null ? null : this.attributeAssignToAssignTo.toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanDelegateAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanDelegateAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot delegate an attribute on attribute assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#assertCanGrantAttributeDefName(String, edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  @Override
  public
  void assertCanGrantAttributeDefName(String action, AttributeDefName attributeDefName) {
    throw new RuntimeException("Cannot grant an attribute on attribute assignment");
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#getAttributeAssignable()
   */
  @Override
  public AttributeAssignable getAttributeAssignable() {
    return this.attributeAssignToAssignTo;
  }

  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeAssignsByOwner()
   */
  @Override
  Set<AttributeAssign> retrieveAttributeAssignsByOwner() {
    
    AttributeAssignType originalAttributeAssignType = this.attributeAssignToAssignTo.getAttributeAssignType();
    AttributeAssignType attributeAssignType = originalAttributeAssignType.getAssignmentOnAssignmentType();
    
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAssignmentsOnAssignments( 
        GrouperUtil.toSet(this.attributeAssignToAssignTo), attributeAssignType, null);

  }


  /**
   * @see edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate#retrieveAttributeDefNamesByOwner()
   */
  @Override
  Set<AttributeDefName> retrieveAttributeDefNamesByOwner() {

    AttributeAssignType originalAttributeAssignType = this.attributeAssignToAssignTo.getAttributeAssignType();
    AttributeAssignType attributeAssignType = originalAttributeAssignType.getAssignmentOnAssignmentType();
    
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAssignmentsOnAssignmentsAttributeDefNames(
          GrouperUtil.toSet(this.attributeAssignToAssignTo), attributeAssignType, null);

  }

}

/**
 * @author mchyzer
 * $Id: AttributeAssignGroupDelegate.java,v 1.1 2009-09-28 05:06:46 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
public class AttributeAssignGroupDelegate {

  /**
   * reference to the attribute def in question
   */
  private Group group = null;
  
  /**
   * 
   * @param group1
   */
  public AttributeAssignGroupDelegate(Group group1) {
    this.group = group1;
  }
  
  /**
   * 
   * @param attributeDefName
   */
  private void assertCanAdminGroupAndUpdateAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canUpdateAttribute = new boolean[1];
    //attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
    final boolean[] canAdminGroup = new boolean[1];
    //this.group.hasAdmin(subject);
 
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canUpdateAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrUpdate(subject);
        canAdminGroup[0] = PrivilegeHelper.canAdmin(rootSession, AttributeAssignGroupDelegate.this.group, subject);
        return null;
      }
    });
    
    if (!canUpdateAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot update attributeDef " + attributeDef.getName());
    }

    if (!canAdminGroup[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot admin group " + group.getName());
    }
  }
  
  /**
   * 
   * @param attributeDefName
   * @return if added or already there
   */
  public boolean assignAttribute(AttributeDefName attributeDefName) {

    this.assertCanAdminGroupAndUpdateAttributeDefName(attributeDefName);

    //see if it exists
    if (this.hasAttributeHelper(attributeDefName, false)) {
      return false;
    }

    AttributeAssign attributeAssign = new AttributeAssign(this.group, AttributeDef.ACTION_DEFAULT, attributeDefName);
    attributeAssign.saveOrUpdate();

    return true;
    
  }

  /**
   * 
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean assignAttributeByName(String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return assignAttribute(attributeDefName);
    
  }

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean assignAttributeById(String attributeDefNameId) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return assignAttribute(attributeDefName);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String attributeDefNameId) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
    .getAttributeDefName().findByIdSecure(attributeDefNameId, true);

    return hasAttribute(attributeDefName);
  }
  
  /**
   * 
   * @param attributeDefName
   * @return true if has attribute, false if not
   */
  public boolean hasAttribute(AttributeDefName attributeDefName) {
    return hasAttributeHelper(attributeDefName, true);
  }

  /**
   * 
   * @param attributeDefName
   * @param checkSecurity 
   * @return true if has attribute, false if not
   */
  private boolean hasAttributeHelper(AttributeDefName attributeDefName, boolean checkSecurity) {
    if (checkSecurity) {
      this.assertCanViewGroupAndReadAttributeDefName(attributeDefName);
    }
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdAndAttributeDefNameId(this.group.getUuid(), attributeDefName.getId());
    return GrouperUtil.length(attributeAssigns) > 0;
  }

  /**
   * see if the group
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String attributeDefNameName) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    
    Set<AttributeAssign> attributeAssigns = retrieveAssignments(attributeDefName);
    return GrouperUtil.length(attributeAssigns) > 0;
  }

  /**
   * @param attributeDefName
   * @return the assignments for a def name
   */
  public Set<AttributeAssign> retrieveAssignments(AttributeDefName attributeDefName) {
    this.assertCanViewGroupAndReadAttributeDefName(attributeDefName);

    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdAndAttributeDefNameId(this.group.getUuid(), attributeDefName.getId());
  }
  
  /**
   * @param attributeDefId
   * @return the assignments for a def name
   */
  public Set<AttributeAssign> retrieveAssignmentsByAttributeDefId(String attributeDefId) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByIdSecure(attributeDefId, true);
    return retrieveAssignments(attributeDef);
  }
  
  /**
   * @param attributeDefId
   * @return the assignments for a def
   */
  public Set<AttributeDefName> retrieveAttributesByAttributeDefId(String attributeDefId) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByIdSecure(attributeDefId, true);
    return retrieveAttributes(attributeDef);
  }
  
  /**
   * @param nameOfAttributeDef
   * @return the attributes for a def
   */
  public Set<AttributeDefName> retrieveAttributesByAttributeDef(String nameOfAttributeDef) {
    AttributeDef attributeDef = GrouperDAOFactory.getFactory()
      .getAttributeDef().findByNameSecure(nameOfAttributeDef, true);
    return retrieveAttributes(attributeDef);
  }
  
  /**
   * @param attributeDef
   * @return the attributes for a def
   */
  public Set<AttributeDefName> retrieveAttributes(AttributeDef attributeDef) {
    this.assertCanViewGroupAndReadAttributeDef(attributeDef);
    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findAttributeDefNamesByGroupIdAndAttributeDefId(this.group.getUuid(), attributeDef.getId());
  }
  
  /**
   * @param name is the name of the attribute def
   * @return the assignments for a def
   */
  public Set<AttributeAssign> retrieveAssignmentsByAttributeDef(String name) {
    
    AttributeDef attributeDef = AttributeDefFinder.findByName(name, true);
    
    return retrieveAssignments(attributeDef);
  }
  
  /**
   * find the assignments of any name associated with a def
   * @param attributeDef
   * @return the set of assignments or the empty set
   */
  public Set<AttributeAssign> retrieveAssignments(AttributeDef attributeDef) {
    this.assertCanViewGroupAndReadAttributeDef(attributeDef);

    return GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdAndAttributeDefId(this.group.getUuid(), attributeDef.getId());

  }

  /**
   * 
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public boolean removeAttribute(AttributeDefName attributeDefName) {
    
    this.assertCanAdminGroupAndUpdateAttributeDefName(attributeDefName);
    
    //see if it exists
    if (!this.hasAttributeHelper(attributeDefName, false)) {
      return false;
    }
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign()
      .findByGroupIdAndAttributeDefNameId(this.group.getId(), attributeDefName.getId());
    for (AttributeAssign attributeAssign : attributeAssigns) {
      attributeAssign.delete();
    }
  
    return true;
    
  }

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean removeAttributeById(String attributeDefNameId) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return removeAttribute(attributeDefName);
  }

  /**
   * 
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean removeAttributeByName(String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return removeAttribute(attributeDefName);
    
  }

  /**
   * 
   * @param attributeDefName
   */
  private void assertCanViewGroupAndReadAttributeDefName(AttributeDefName attributeDefName) {
    final AttributeDef attributeDef = attributeDefName.getAttributeDef();
    assertCanViewGroupAndReadAttributeDef(attributeDef);
  }

  /**
   * 
   * @param attributeDef
   */
  private void assertCanViewGroupAndReadAttributeDef(final AttributeDef attributeDef) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession.getSubject();
    final boolean[] canReadAttribute = new boolean[1];
    final boolean[] canViewGroup = new boolean[1];
  
    //these need to be looked up as root
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      /**
       * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
       */
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        canReadAttribute[0] = attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        canViewGroup[0] = PrivilegeHelper.canView(rootSession, AttributeAssignGroupDelegate.this.group, subject);
        return null;
      }
    });
    
    if (!canReadAttribute[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot read attributeDef " + attributeDef.getName());
    }
  
    if (!canViewGroup[0]) {
      throw new InsufficientPrivilegeException("Subject " + GrouperUtil.subjectToString(subject) 
          + " cannot view group " + group.getName());
    }
  }
  
}

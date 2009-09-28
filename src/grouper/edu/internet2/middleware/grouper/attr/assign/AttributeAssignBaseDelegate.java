/**
 * @author mchyzer
 * $Id: AttributeAssignBaseDelegate.java,v 1.1 2009-09-28 06:05:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * delegate privilege calls from attribute defs
 */
public abstract class AttributeAssignBaseDelegate {

  /**
   */
  AttributeAssignBaseDelegate() {
    //empty
  }
  
  /**
   * make sure the user can read the attribute (including looking at object if necessary)
   * @param attributeDefName
   */
  void assertCanReadAttributeDefName(AttributeDefName attributeDefName) {
    AttributeDef attributeDef = attributeDefName.getAttributeDef();
    assertCanReadAttributeDef(attributeDef);
  }

  /**
   * make sure the user can read the attribute (including looking at object if necessary)
   * @param attributeDef
   */
  abstract void assertCanReadAttributeDef(AttributeDef attributeDef);

  /**
   * make sure the user can update the attribute (including looking at object if necessary)
   * @param attributeDefName
   */
  abstract void assertCanUpdateAttributeDefName(AttributeDefName attributeDefName);

  /**
   * check security and assign attribute
   * @param attributeDefName
   * @return if added or already there
   */
  public abstract boolean assignAttribute(AttributeDefName attributeDefName);

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
  abstract boolean hasAttributeHelper(AttributeDefName attributeDefName, boolean checkSecurity);

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
  public abstract Set<AttributeAssign> retrieveAssignments(AttributeDefName attributeDefName);  

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
  public abstract Set<AttributeDefName> retrieveAttributes(AttributeDef attributeDef);
  
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
  public abstract Set<AttributeAssign> retrieveAssignments(AttributeDef attributeDef);

  /**
   * 
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public abstract boolean removeAttribute(AttributeDefName attributeDefName);
  
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

  
}

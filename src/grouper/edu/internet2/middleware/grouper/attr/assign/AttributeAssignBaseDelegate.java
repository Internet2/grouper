/**
 * @author mchyzer
 * $Id: AttributeAssignBaseDelegate.java,v 1.3 2009-10-05 00:50:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


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
   * @param action is the action on the attribute assignment (e.g. read, write, assign [default])
   * if null, should go to default
   * @param attributeDefName
   * @return attribute assign
   */
  abstract AttributeAssign newAttributeAssign(String action, AttributeDefName attributeDefName);
  
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
   * 
   * @param attributeDefName
   * @return if added or already there
   */
  public boolean assignAttribute(AttributeDefName attributeDefName) {

    return assignAttribute(null, attributeDefName);
  }

  /**
   * 
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean assignAttributeByName(String attributeDefNameName) {
    return assignAttributeByName(null, attributeDefNameName);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean assignAttributeById(String attributeDefNameId) {
    return this.assignAttributeById(null, attributeDefNameId);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String attributeDefNameId) {
    return hasAttributeById(null, attributeDefNameId);
  }
  
  /**
   * 
   * @param attributeDefName
   * @return true if has attribute, false if not
   */
  public boolean hasAttribute(AttributeDefName attributeDefName) {
    return hasAttribute(null, attributeDefName);
  }

  /**
   * 
   * @param action on the assignment
   * @param attributeDefName
   * @param checkSecurity 
   * @return true if has attribute, false if not
   */
  boolean hasAttributeHelper(String action, AttributeDefName attributeDefName, boolean checkSecurity) {
    if (checkSecurity) {
      this.assertCanReadAttributeDefName(attributeDefName);
    }
    Set<AttributeAssign> attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
    
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = StringUtils.defaultIfEmpty(attributeAssign.getAction(), AttributeDef.ACTION_DEFAULT);
      if (StringUtils.equals(action, currentAttributeAction)) {
        return true;
      }
    }

    return false;
  }


  /**
   * see if the group
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String attributeDefNameName) {
    return hasAttributeByName(null, attributeDefNameName);
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
    this.assertCanReadAttributeDef(attributeDef);

    return retrieveAttributeAssignsByOwnerAndAttributeDefId(attributeDef.getId());

  }

  /**
   * @param attributeDefName
   * @return the assignments for a def name
   */
  public Set<AttributeAssign> retrieveAssignments(AttributeDefName attributeDefName) {
    this.assertCanReadAttributeDefName(attributeDefName);

    return retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
  }

  /**
   * @param attributeDef
   * @return the attributes for a def
   */
  public Set<AttributeDefName> retrieveAttributes(AttributeDef attributeDef) {
    this.assertCanReadAttributeDef(attributeDef);
    return retrieveAttributeDefNamesByOwnerAndAttributeDefId(attributeDef.getId());
  }
  

  /**
   * 
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public boolean removeAttribute(AttributeDefName attributeDefName) {
    return removeAttribute(null, attributeDefName);
  }

  /**
   * get attribute assigns by owner and attribute def name id
   * @param attributeDefNameId
   * @return set of assigns or empty if none there
   */
  abstract Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefNameId(String attributeDefNameId);
  
  /**
   * get attribute assigns by owner and attribute def id
   * @param attributeDefId
   * @return set of assigns or empty if none there
   */
  abstract Set<AttributeAssign> retrieveAttributeAssignsByOwnerAndAttributeDefId(String attributeDefId);

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean removeAttributeById(String attributeDefNameId) {
    return removeAttributeById(null, attributeDefNameId);
  }

  /**
   * 
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean removeAttributeByName(String attributeDefNameName) {
    return removeAttributeByName(null, attributeDefNameName);
  }

  /**
   * get attribute def names by owner and attribute def id
   * @param attributeDefId
   * @return set of def names or empty if none there
   */
  abstract Set<AttributeDefName> retrieveAttributeDefNamesByOwnerAndAttributeDefId(String attributeDefId);

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return if added or already there
   */
  public boolean assignAttribute(String action, AttributeDefName attributeDefName) {
    this.assertCanUpdateAttributeDefName(attributeDefName);

    //see if it exists
    if (this.hasAttributeHelper(action, attributeDefName, false)) {
      return false;
    }

    AttributeAssign attributeAssign = newAttributeAssign(action, attributeDefName);
    attributeAssign.saveOrUpdate();

    return true;

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean assignAttributeById(String action, String attributeDefNameId) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return assignAttribute(action, attributeDefName);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean assignAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return assignAttribute(action, attributeDefName);

  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return true if has attribute, false if not
   */
  public boolean hasAttribute(String action, AttributeDefName attributeDefName) {
    return hasAttributeHelper(action, attributeDefName, true);
  }

  /**
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String action, String attributeDefNameId) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);

    return hasAttribute(action, attributeDefName);

  }

  /**
   * see if the group
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    
    Set<AttributeAssign> attributeAssigns = retrieveAssignments(attributeDefName);
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = StringUtils.defaultIfEmpty(attributeAssign.getAction(), AttributeDef.ACTION_DEFAULT);
      if (StringUtils.equals(action, currentAttributeAction)) {
        return true;
      }
    }
    return false;

  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefName
   * @return if removed or already not assigned
   */
  public boolean removeAttribute(String action, AttributeDefName attributeDefName) {
    this.assertCanUpdateAttributeDefName(attributeDefName);
    
    //see if it exists
    if (!this.hasAttributeHelper(action, attributeDefName, false)) {
      return false;
    }
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    Set<AttributeAssign> attributeAssigns = retrieveAttributeAssignsByOwnerAndAttributeDefNameId(attributeDefName.getId());
    for (AttributeAssign attributeAssign : attributeAssigns) {
      String currentAttributeAction = StringUtils.defaultIfEmpty(attributeAssign.getAction(), AttributeDef.ACTION_DEFAULT);
      if (StringUtils.equals(action, currentAttributeAction)) {
        attributeAssign.delete();
      }
    }
  
    return true;
    

  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean removeAttributeById(String action, String attributeDefNameId) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByIdSecure(attributeDefNameId, true);
    return removeAttribute(action, attributeDefName);
  }

  /**
   * 
   * @param action is the action on the assignment (null means default action)
   * @param attributeDefNameName
   * @return if added or already there
   */
  public boolean removeAttributeByName(String action, String attributeDefNameName) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByNameSecure(attributeDefNameName, true);
    return removeAttribute(action, attributeDefName);
  
  }

  
}

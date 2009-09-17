/**
 * @author mchyzer
 * $Id: AttributeDefNameSetDelegate.java,v 1.1 2009-09-17 22:40:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import java.io.Serializable;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * delegate the attribute def name set
 */
@SuppressWarnings("serial")
public class AttributeDefNameSetDelegate implements Serializable {

  /** keep a reference to the attribute def name */
  private AttributeDefName attributeDefName;
  
  /**
   * 
   * @param attributeDefName1
   */
  public AttributeDefNameSetDelegate(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;

  }

  /**
   * get all the THEN rows from attributeDefNameSet about this id.  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThis() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet().attributeDefNamesImpliedByThis(this.attributeDefName.getId());
  }

  /**
   * get all the THEN rows from attributeDefNameSet about this id (immediate only).  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesImpliedByThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet().attributeDefNamesImpliedByThisImmediate(this.attributeDefName.getId());
  }

  /**
   * get all the IF rows from attributeDefNameSet about this id.  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThis() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .attributeDefNamesThatImplyThis(this.attributeDefName.getId());
  }

  /**
   * get all the IF rows from attributeDefNameSet about this id (immediate only).  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeDefNames, or empty set if none available
   */
  public Set<AttributeDefName> getAttributeDefNamesThatImplyThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .attributeDefNamesThatImplyThisImmediate(this.attributeDefName.getId());
  }

  /**
   * 
   * @param newAttributeDefName
   * @return true if added, false if already there
   */
  public boolean addToAttributeDefNameSet(AttributeDefName newAttributeDefName) {
    return GrouperSetEnum.ATTRIBUTE_SET.addToGrouperSet(this.attributeDefName, newAttributeDefName);
  }

  /**
   * 
   * @param attributeDefNameToRemove
   * @return true if removed, false if already removed
   */
  public boolean removeFromAttributeDefNameSet(AttributeDefName attributeDefNameToRemove) {
    return GrouperSetEnum.ATTRIBUTE_SET.removeFromGrouperSet(this.attributeDefName, attributeDefNameToRemove);
  }

}

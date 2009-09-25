/**
 * @author mchyzer
 * $Id: AttributeAssignGroupDelegate.java,v 1.1 2009-09-25 06:04:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
   * @return if added or already there
   */
  public boolean assignAttribute(AttributeDefName attributeDefName) {
    
    //see if it exists
    if (this.hasAttributeById(attributeDefName.getId())) {
      return false;
    }
    
    AttributeAssign attributeAssign = new AttributeAssign(this.group, AttributeDef.ACTION_DEFAULT, attributeDefName);
    attributeAssign.saveOrUpdate();

    return true;
    
  }

  /**
   * 
   * @param attributeDefNameId
   * @return if added or already there
   */
  public boolean assignAttributeById(String attributeDefNameId) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findById(attributeDefNameId, true);
    return assignAttribute(attributeDefName);
  }

  /**
   * 
   * @param attributeDefNameId
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeById(String attributeDefNameId) {
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdAndAttributeDefNameId(this.group.getUuid(), attributeDefNameId);
    return GrouperUtil.length(attributeAssigns) > 0;
  }
  
  /**
   * see if the group
   * @param attributeDefNameName
   * @return true if has attribute, false if not
   */
  public boolean hasAttributeByName(String attributeDefNameName) {
    
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory()
      .getAttributeDefName().findByName(attributeDefNameName, true);
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByGroupIdAndAttributeDefNameId(this.group.getUuid(), attributeDefName.getId());
    return GrouperUtil.length(attributeAssigns) > 0;
  }
  
}

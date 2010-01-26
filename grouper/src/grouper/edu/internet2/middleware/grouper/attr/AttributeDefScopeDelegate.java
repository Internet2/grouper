/**
 * @author mchyzer
 * $Id: AttributeAssignAttributeDefDelegate.java,v 1.3 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * delegate scope calls from attribute defs
 */
public class AttributeDefScopeDelegate {

  /**
   * reference to the attributedef in question
   */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   * @param attributeDef1
   */
  public AttributeDefScopeDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }
  
  /**
   * 
   * @param attributeDefScopeType 
   * @param scopeString 
   * @param scopeString2 
   * @return attribute assign
   */
  public AttributeDefScope newScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2) {
    return internal_newScope(attributeDefScopeType, scopeString, scopeString2, null);
  }
  
  /**
   * @param attributeDefScopeType 
   * @param scopeString 
   * @param scopeString2 
   * @param uuid is uuid or null for generated
   * @return attribute assign
   */
  public AttributeDefScope internal_newScope(AttributeDefScopeType attributeDefScopeType, String scopeString, String scopeString2, String uuid) {
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setAttributeDefScopeType(attributeDefScopeType);
    attributeDefScope.setScopeString(scopeString);
    attributeDefScope.setScopeString2(scopeString2);
    attributeDefScope.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);
    attributeDefScope.setAttributeDefId(this.attributeDef.getId());
    return attributeDefScope;
  }

  /**
   * 
   * @return scopes (non null)
   */
  public Set<AttributeDefScope> retrieveAttributeDefScopes() {
    return GrouperDAOFactory.getFactory().getAttributeDefScope().findByAttributeDefId(this.attributeDef.getId());
  }

}

/**
 * @author mchyzer
 * $Id: AttributeDefFinder.java,v 1.1 2009-09-28 05:06:46 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * finder methods for attribute def
 */
public class AttributeDefFinder {

  /**
   * find an attributeDef by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   */
  public static AttributeDef findById(String id, boolean exceptionIfNull) {
    
    
    AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findByIdSecure(id, exceptionIfNull);
    
    return attributeDef;
    
  }
  
  /**
   * find an attributeDef by name.  This is a secure method, a GrouperSession must be open
   * @param name of attributeDef
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   */
  public static AttributeDef findByName(String name, boolean exceptionIfNull) {
    return GrouperDAOFactory.getFactory().getAttributeDef().findByNameSecure(name, exceptionIfNull);
  }
  
  
  
}

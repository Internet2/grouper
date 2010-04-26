/**
 * @author mchyzer
 * $Id: AttributeDefNameFinder.java,v 1.1 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * finder methods for attribute def name
 */
public class AttributeDefNameFinder {

  /**
   * find an attributeDefName by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeDefName
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def or null
   * @throws AttributeDefNameNotFoundException
   */
  public static AttributeDefName findById(String id, boolean exceptionIfNull) {
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(id, exceptionIfNull);
    return attributeDefName;
  }
  
  /**
   * find an attributeDefName by name.  This is a secure method, a GrouperSession must be open
   * @param name of attributeDefName
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute def name or null
   * @throws AttributeDefNameNotFoundException
   */
  public static AttributeDefName findByName(String name, boolean exceptionIfNull) {
    return GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure(name, exceptionIfNull);
  }
  
  /**
   * search for attributeDefName by name, display name, or description.  This is a secure method, a GrouperSession must be open.
   * This will add the % to the input to look for a substring
   * @param searchField substring to search for
   * @param searchInAttributeDefIds ids to search in or null for all
   * @return the attribute def names or empty set
   */
  public static Set<AttributeDefName> findAll(String searchField, Set<String> searchInAttributeDefIds) {
    
    if (!StringUtils.isBlank(searchField)) {
      searchField = "%" + searchField + "%";
    }
    
    return GrouperDAOFactory.getFactory().getAttributeDefName().findAllSecure(searchField, searchInAttributeDefIds);
  }
  
}

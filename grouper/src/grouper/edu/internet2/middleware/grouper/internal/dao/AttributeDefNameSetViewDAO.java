/*
 * @author mchyzer
 * $Id: AttributeDefNameSetViewDAO.java,v 1.1 2009-07-03 21:15:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSetView;

/**
 * attribute def name set views, links up attributes with other attributes (probably for privs)
 */
public interface AttributeDefNameSetViewDAO extends GrouperDAO {
  
  /**
   * find all attribute def name set views by related attribute def names (generally this is for testing)
   * @param attributeDefNames
   * @return the attr def name set views
   */
  public Set<AttributeDefNameSetView> findByAttributeDefNameSetViews(Set<String> attributeDefNames);
  
}

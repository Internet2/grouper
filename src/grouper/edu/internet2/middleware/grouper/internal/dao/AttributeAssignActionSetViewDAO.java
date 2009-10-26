/*
 * @author mchyzer
 * $Id: AttributeAssignActionSetViewDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSetView;

/**
 * attribute assign action set views, links up actions with other actions (probably for privs)
 */
public interface AttributeAssignActionSetViewDAO extends GrouperDAO {
  
  /**
   * find all attribute assign action set views by related attribute assign actions (generally this is for testing)
   * @param attributeAssignActions
   * @return the attr def name set views
   */
  public Set<AttributeAssignActionSetView> findByAttributeAssignActionSetViews(Set<String> attributeAssignActions);
  
}

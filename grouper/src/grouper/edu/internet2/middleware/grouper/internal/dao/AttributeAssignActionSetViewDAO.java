/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.filter;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;


/** 
 * Query by approximate group name 
 * (current group name, display name, extension, display extension only).
 * <p/>
 * @author shilen
 * @version $Id: GroupCurrentNameFilter.java,v 1.1 2009-03-27 19:32:41 shilen Exp $
 */
public class GroupCurrentNameFilter extends BaseQueryFilter {

  // Private Instance Variables
  private String  name;
  private Stem    ns;


  // Constructors

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * name.
   * <p>
   * This performs a substring, lowercased query against <i>name</i>,
   * <i>displayName</i>, <i>extension</i> and <i>displayExtension</i>
   * </p>
   * @param   name  Find groups matching this name.
   * @param   ns    Restrict results to within this stem.
   */
  public GroupCurrentNameFilter(String name, Stem ns) {
    this.name = name;
    this.ns   = ns;
  }


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results;  
  
    if (ns.isRootStem()) {
      results = PrivilegeHelper.canViewGroups(
        s, GrouperDAOFactory.getFactory().getGroup().findAllByApproximateCurrentName(this.name));
    } else {
      results = PrivilegeHelper.canViewGroups(
        s, GrouperDAOFactory.getFactory().getGroup().findAllByApproximateCurrentName(this.name, getStringForScope(ns)));
    }
    return results;
  } // public Set getResults(s)

}


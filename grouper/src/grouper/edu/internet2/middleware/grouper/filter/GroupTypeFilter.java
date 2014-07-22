/**
 * Copyright 2012 Internet2
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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * Query by {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupTypeFilter.java,v 1.2 2008-11-04 15:19:56 shilen Exp $
 * @since   1.2.0
 */
public class GroupTypeFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private GroupType type  = null;
  private Stem      ns    = null;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns groups that have the specified 
   * {@link GroupType}.
   * @param   type  Find groups of this type.
   * @param   ns    Restrict results to within this stem.
   * @since   1.2.0
   */
  public GroupTypeFilter(GroupType type, Stem ns) {
    this.type = type;
    this.ns   = ns;
  } // public GroupTypeFilter(type, ns)


  // PUBLIC INSTANCE METHODS //

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set       groups  = new LinkedHashSet();
    Group     g;  
    Iterator  it;

    if (ns.isRootStem()) {
      it = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.type).iterator();
    } else {
      it = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.type, getStringForScope(ns)).iterator();
    }

    while (it.hasNext()) {
      g = (Group) it.next();
      groups.add(g);
    }
    return groups;
  } 

}


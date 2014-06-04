/*******************************************************************************
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
 ******************************************************************************/
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


/** 
 * Query by all group attributes.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupAnyAttributeFilter.java,v 1.3 2008-11-04 15:19:56 shilen Exp $
 */
public class GroupAnyAttributeFilter extends BaseQueryFilter {

  // Private Instance Variables
  private Stem    ns;
  private String  val;


  // Constructors

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * attribute specification.
   * <p>
   * This performs a substring, lowercased query against all
   * attributes.
   * </p>
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public GroupAnyAttributeFilter(String value, Stem ns) {
    this.ns   = ns;
    this.val  = value;
  } // public GroupAnyAttributeFilter(value, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results;

    if (ns.isRootStem()) {
      results = GrouperDAOFactory.getFactory().getGroup().findAllByAnyApproximateAttr(this.val, null, true);
    } else {
      results = GrouperDAOFactory.getFactory().getGroup().findAllByAnyApproximateAttr(this.val, getStringForScope(ns), true);
    }
    return results;
  } // public Set getResults(s)

}


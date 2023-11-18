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


/** 
 * Query by group attribute exactly, not with like or lower.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupAttributeExactFilter.java,v 1.2 2008-11-04 15:19:56 shilen Exp $
 */
public class GroupAttributeExactFilter extends BaseQueryFilter {

  // Private Instance Variables
  private String  attr;
  private Stem    ns;
  private String  val;

  /** true if enabled only, false if disabled only, null if everything */
  private Boolean enabled = true;

  // Constructors

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * attribute specification exactly, not with like or lower.
   * <p>
   * This performs a substring, lowercased query on <i>attribute</i>.
   * </p>
   * @param   attr  Search on this attribute.
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public GroupAttributeExactFilter(String attr, String value, Stem ns) {
    this.attr = attr;
    this.ns   = ns;
    this.val  = value;
  } // public GroupAttributeFilter(attr, value, ns)


  /**
   * {@link QueryFilter} that returns groups matching the specified
   * attribute specification exactly, not with like or lower.
   * <p>
   * This performs a substring, lowercased query on <i>attribute</i>.
   * </p>
   * @param   attr  Search on this attribute.
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   * @param   enabled
   */
  public GroupAttributeExactFilter(String attr, String value, Stem ns, Boolean enabled) {
    this.attr = attr;
    this.ns   = ns;
    this.val  = value;
    this.enabled = enabled;
  }
  
  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set results;

    if (ns.isRootStem()) {
      results = GrouperDAOFactory.getFactory().getGroup().findAllByAttr(this.attr, this.val, null, true, enabled);
    } else {
      results = GrouperDAOFactory.getFactory().getGroup().findAllByAttr(this.attr, this.val, getStringForScope(ns), true, enabled);
    }
    return results;
  } // public Set getResults(s)

  

  /**
   * @return true if enabled only, false if disabled only, null if everything
   */
  public Boolean getEnabled() {
    return enabled;
  }

  
  /**
   * @param enabled true if enabled only, false if disabled only, null if everything
   */
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
}


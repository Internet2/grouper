/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;

import  java.util.*;


/** 
 * Query by group attribute.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupAttributeFilter.java,v 1.3 2006-06-15 04:45:58 blair Exp $
 */
public class GroupAttributeFilter extends BaseQueryFilter {

  // Private Instance Variables
  private String  attr;
  private Stem    ns;
  private String  val;


  // Constructors

  /**
   * {@link QueryFilter} that returns groups matching the specified
   * attribute specification.
   * <p>
   * This performs a substring, lowercased query on <i>attribute</i>.
   * </p>
   * @param   attr  Search on this attribute.
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public GroupAttributeFilter(String attr, String value, Stem ns) {
    this.attr = attr;
    this.ns   = ns;
    this.val  = value;
  } // public GroupAttributeFilter(attr, value, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = GroupFinder.findByApproximateAttr(s, this.attr, this.val);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}


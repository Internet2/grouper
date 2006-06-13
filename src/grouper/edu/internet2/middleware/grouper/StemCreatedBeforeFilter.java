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
 * Query by stems created after the specified date.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemCreatedBeforeFilter.java,v 1.5 2006-06-13 20:01:32 blair Exp $
 */
public class StemCreatedBeforeFilter extends BaseQueryFilter {

  // Private Instance Variables
  private Date  d;      
  private Stem  ns;


  // Constructors

  /**
   * {@link QueryFilter} that returns stems created after the
   * specified date. 
   * <p/>
   * @param   d   Find stems created after this date.
   * @param   ns  Restrict results to within this stem.
   */
  public StemCreatedBeforeFilter(Date d, Stem ns) {
    this.d  = (Date) d.clone();
    this.ns = ns;
  } // public StemCreatedBeforeFilter(d, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = StemFinder.findByCreatedBefore(s, this.d);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}


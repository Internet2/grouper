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

package edu.internet2.middleware.grouper;
import  java.util.*;

/** 
 * Query by stem attribute.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemNameFilter.java,v 1.12 2008-06-24 06:07:03 mchyzer Exp $
 */
public class StemNameFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private Stem    ns;
  private String  val;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * <i>name</i> value.
   * <p>
   * This performs a substring, lowercased query on <i>name</i>.
   * </p>
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public StemNameFilter(String value, Stem ns) {
    this.ns   = ns;
    this.val  = value;
  } // public StemNameFilter(value, ns)


  // PUBLIC INSTANCE METHODS //
  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set candidates  = StemFinder.internal_findAllByApproximateName(s, this.val);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}


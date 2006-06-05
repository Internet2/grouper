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
 * Query by stem attribute.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemExtensionFilter.java,v 1.3 2006-06-05 19:54:40 blair Exp $
 */
public class StemExtensionFilter extends BaseQueryFilter {

  // PRIVATE INSTANCE VARIABLES //
  private Stem    ns;
  private String  val;


  // CONSTRUCTORS //

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * <i>extension</i> value.
   * <p>
   * This performs a substring, lowercased query on <i>extension</i>.
   * </p>
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public StemExtensionFilter(String value, Stem ns) {
    this.ns   = ns;
    this.val  = value;
  } // public StemExtensionFilter(value, ns)


  // PUBLIC INSTANCE METHODS //
  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = StemFinder.findByApproximateExtension(s, this.val);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}


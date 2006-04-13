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
import  org.apache.commons.logging.*;


/** 
 * Query by stem attribute.
 * <p />
 * @author  blair christensen.
 * @version $Id: StemDisplayExtensionFilter.java,v 1.1.2.1 2006-04-13 19:52:49 blair Exp $
 */
public class StemDisplayExtensionFilter extends BaseQueryFilter {

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(StemDisplayExtensionFilter.class);

  // Private Instance Variables //
  private Stem    ns;
  private String  val;


  // Constructors //

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * <i>displayExtension</i> value.
   * <p>
   * This performs a substring, lowercased query on <i>displayExtension</i>.
   * </p>
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public StemDisplayExtensionFilter(String value, Stem ns) {
    this.ns   = ns;
    this.val  = value;
  } // public StemDisplayExtensionFilter(value, ns)


  // Public Instance Methods //
  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSession.validate(s);
    Set candidates  = StemFinder.findByApproximateDisplayExtension(s, this.val);
    Set results     = this.filterByScope(this.ns, candidates);
    return results;
  } // public Set getResults(s)

}


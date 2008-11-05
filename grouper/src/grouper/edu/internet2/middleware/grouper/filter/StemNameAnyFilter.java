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
 * Query by stem name.
 * <p/>
 * @author  blair christensen.
 * @version $Id: StemNameAnyFilter.java,v 1.3 2008-11-05 16:18:46 shilen Exp $
 */
public class StemNameAnyFilter extends BaseQueryFilter {

  // Private Instance Variables
  private String  name;
  private Stem    ns;


  // Constructors

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * name.
   * <p>
   * This performs a substring, lowercased query against <i>name</i>,
   * <i>displayName</i>, <i>extension</i> and <i>displayExtension</i>.
   * </p>
   * @param   name  Find stems matching this name.
   * @param   ns    Restrict results to within this stem.
   */
  public StemNameAnyFilter(String name, Stem ns) {
    this.name = name;
    this.ns   = ns;
  } // public StemNameAnyFilter(name, ns)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results;
    if (ns.isRootStem()) {
      results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(this.name));
    } else {
      results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(this.name, getStringForScope(ns));
    }
    return results;
  } // public Set getResults(s)

}


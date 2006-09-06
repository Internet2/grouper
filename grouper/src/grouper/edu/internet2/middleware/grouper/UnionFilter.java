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
 * Returns the union of two other query filters.
 * <p/>
 * @author  blair christensen.
 * @version $Id: UnionFilter.java,v 1.6 2006-09-06 15:30:40 blair Exp $
 */
public class UnionFilter extends BaseQueryFilter {

  // Private Instance Variables
  private QueryFilter a;
  private QueryFilter b;


  // Constructors

  /**
   * {@link QueryFilter} that returns the union of two other query
   * filters.
   * <p/>
   * @param   a   First query filter
   * @param   b   Second query filter
   */
  public UnionFilter(QueryFilter a, QueryFilter b) {
    this.a = a;
    this.b = b;
  } // public UnionFilter(a, b)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    GrouperSessionValidator.validate(s);
    Set results = new LinkedHashSet();
    results.addAll( this.a.getResults(s) );
    results.addAll( this.b.getResults(s) );
    return results;
  } // public Set getResults(s)

}


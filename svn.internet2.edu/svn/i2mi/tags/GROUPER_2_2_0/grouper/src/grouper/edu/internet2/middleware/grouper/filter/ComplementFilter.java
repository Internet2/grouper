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

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.QueryException;


/** 
 * Returns the compliment of two other query filters.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ComplementFilter.java,v 1.2 2008-07-21 05:32:20 mchyzer Exp $
 */
public class ComplementFilter extends BaseQueryFilter {

  // Private Instance Variables
  private QueryFilter a;
  private QueryFilter b;


  // Constructors

  /**
   * {@link QueryFilter} that returns the compliment of two other query
   * filters.
   * <p/>
   * @param   a   First query filter
   * @param   b   Second query filter
   */
  public ComplementFilter(QueryFilter a, QueryFilter b) {
    this.a = a;
    this.b = b;
  } // public ComplementFilter(a, b)


  // Public Instance Methods

  public Set getResults(GrouperSession s) 
    throws QueryException
  {    
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results = new LinkedHashSet();
    results.addAll( this.a.getResults(s) );
    results.removeAll( this.b.getResults(s) );
    return results;
  } // public Set getResults(s)

}


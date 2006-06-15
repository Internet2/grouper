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
 * Interface for querying the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: QueryFilter.java,v 1.4 2006-06-15 00:07:02 blair Exp $
 */
public interface QueryFilter {

  // Public Instance Methods

  /**
   * Get filter results.
   * <p/>
   * @param   s   Get groups within this session context.
   * @return  Objects that match filter constraints.
   * @throws  QueryException
   */
  Set getResults(GrouperSession s) throws QueryException;

}


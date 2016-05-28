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

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;


/** 
 * Query by exact stem name.
 * 
 * @author  blair christensen.
 * @version $Id: StemNameExactFilter.java,v 1.2 2009-03-15 06:37:22 mchyzer Exp $
 */
public class StemNameExactFilter extends BaseQueryFilter {

  // Private Instance Variables
  /** exact name of stem to find */
  private String  name;

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * name exactly.
   * @param   name1  Find stems matching this name.
   */
  public StemNameExactFilter(String name1) {
    this.name = name1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   * @return the stem in a set, or null if none
   */
  @Override
  public Set<Stem> getResults(GrouperSession s) 
    throws QueryException  {

    //note, no need for GrouperSession inverse of control

    GrouperSession.validate(s);
    Set candidates  = new HashSet<Stem>();
    Stem stem = null;
    try {
      stem = StemFinder.findByName(s, this.name, true);
      candidates.add(stem);
    } catch (StemNotFoundException gnfe) {
      return candidates;
      //ignore
    }
    return candidates;
  } 
}


/**
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
 * Query by stem uuid.
 * <p/>
 * @author  mchyzer.
 * @version $Id: StemUuidFilter.java,v 1.2 2009-03-15 06:37:22 mchyzer Exp $
 */
public class StemUuidFilter extends BaseQueryFilter {

  /** uuid of group to find */
  private String uuid;

  // Constructors

  /**
   * {@link QueryFilter} that returns stem matching the specified
   * uuid.
   * @param   theUuid  Find groups matching this uuid.
   */
  public StemUuidFilter(String theUuid) {
    this.uuid = theUuid;
  }

  /**
   * get the results
   * @param s is the grouper session
   * @return the set of groups (which is just going to be one or not groups)
   * @throws QueryException
   */
  public Set<Stem> getResults(GrouperSession s) 
      throws QueryException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Stem> stems  = new HashSet<Stem>();
    Stem stem = null;
    try {
      stem = StemFinder.findByUuid(s, this.uuid, true);
      stems.add(stem);
    } catch (StemNotFoundException gnfe) {
      //ignore
    }
    return stems;
  }
}


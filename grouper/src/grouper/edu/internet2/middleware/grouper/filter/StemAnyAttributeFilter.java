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

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/** 
 * Query by all stem attributes.
 * <p/>
 * @author  mchyzer
 * @version $Id: StemAnyAttributeFilter.java,v 1.2 2008-11-05 16:18:46 shilen Exp $
 */
public class StemAnyAttributeFilter extends BaseQueryFilter {
  
  /** stem to filter results in */
  private Stem    ns;
  
  /** value of any attribute */
  private String  val;


  /**
   * {@link QueryFilter} that returns stems matching the specified
   * attribute specification.
   * <p>
   * This performs a substring, lowercased query against all
   * attributes.
   * </p>
   * @param   value Search for this value.
   * @param   ns    Restrict results to within this stem.
   */
  public StemAnyAttributeFilter(String value, Stem ns) {
    this.ns   = ns;
    this.val  = value;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   */
  public Set getResults(GrouperSession s) throws QueryException {
    GrouperSession.validate(s);
    Set results = (Set)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Set results;
        if (ns.isRootStem()) {
          results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(StemAnyAttributeFilter.this.val));
        } else {
          results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(StemAnyAttributeFilter.this.val, getStringForScope(ns));
        }
        return results;
      }
      
    });
    return results;
  } // public Set getResults(s)

}


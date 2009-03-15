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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Query by stem name exact, and get all children, or just immediate
 * <p/>
 * @author  mchyzer
 * @version $Id: GroupsInStemFilter.java,v 1.2 2009-03-15 06:37:22 mchyzer Exp $
 */
public class GroupsInStemFilter extends BaseQueryFilter {
  
  /** stem name to use */
  private String  stemName;
  
  /** if getting all children or just immediate, defaults to immediate */
  private Scope scope;

  /** if we should throw QueryException if stem not found */
  private boolean failOnStemNotFound;

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * <i>name</i> value.
   * <p>
   * This performs a substring, lowercased query on <i>name</i>.
   * </p>
   * @param theStemName is the name (exact) of the stem to search
   * @param theScope is the type of children to return (all or immediate)
   * @param theFailOnStemNotFound true if GrouperException should be thrown on StemNotFoundException
   */
  public GroupsInStemFilter(String theStemName, Scope theScope,
      boolean theFailOnStemNotFound) {
    this.stemName = theStemName;
    this.scope = GrouperUtil.defaultIfNull(theScope, 
        Scope.ONE);
    this.failOnStemNotFound = theFailOnStemNotFound;
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   */
  public Set<Group> getResults(GrouperSession s) throws QueryException {
    
    GrouperSession.validate(s);
    Set<Group> groups = null;
    //first find the stem.
    final Stem stem;
    try {
      stem = StemFinder.findByName(s, this.stemName, true);
    } catch (StemNotFoundException stfe) {
      if (this.failOnStemNotFound) {
        throw new QueryException("Stem not found: '" + this.stemName + "'");
      }
      //if not found, and not supposed to fail, then just return
      return new HashSet<Group>();
    }
    groups = (Set<Group>)GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        //based on which children, find them
        Set<Group> groups = stem.getChildGroups(GroupsInStemFilter.this.scope);
        return groups;
      }
      
    });
    
    return groups;
  }

}


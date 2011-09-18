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
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** 
 * Query by stem name exact, and get all stem children, or just immediate
 * <p/>
 * @author  mchyzer
 * @version $Id: StemsInStemFilter.java,v 1.2 2009-03-15 06:37:22 mchyzer Exp $
 */
public class StemsInStemFilter extends BaseQueryFilter {
  
  /** stem name to use */
  private String  stemName;
  
  /** if getting all children or just immediate, defaults to immediate */
  private Scope scope;

  /** if we should throw QueryException if stem not found */
  private boolean failOnStemNotFound;

  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private Boolean ascending;

  /** page number 1 indexed if paging */
  private Integer pageNumber;

  /** page size if paging */
  private Integer pageSize;

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

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
  public StemsInStemFilter(String theStemName, Scope theScope,
      boolean theFailOnStemNotFound) {
    this.stemName = theStemName;
    this.scope = GrouperUtil.defaultIfNull(theScope, 
        Scope.ONE);
    this.failOnStemNotFound = theFailOnStemNotFound;
  }

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * <i>name</i> value.
   * <p>
   * This performs a substring, lowercased query on <i>name</i>.
   * </p>
   * @param theStemName is the name (exact) of the stem to search
   * @param theScope is the type of children to return (all or immediate)
   * @param theFailOnStemNotFound true if GrouperException should be thrown on StemNotFoundException
   * @param theSortString 
   * @param theAscending 
   * @param thePageNumber 
   * @param thePageSize 
   */
  public StemsInStemFilter(String theStemName, Scope theScope,
      boolean theFailOnStemNotFound, String theSortString, 
      Boolean theAscending, Integer thePageNumber, Integer thePageSize) {
    this.stemName = theStemName;
    this.scope = GrouperUtil.defaultIfNull(theScope, 
        Scope.ONE);
    this.failOnStemNotFound = theFailOnStemNotFound;
    this.sortString = theSortString;
    this.ascending = theAscending;
    this.pageNumber = thePageNumber;
    this.pageSize = thePageSize;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.filter.BaseQueryFilter#getResults(edu.internet2.middleware.grouper.GrouperSession)
   */
  public Set<Stem> getResults(GrouperSession s) throws QueryException {
    
    GrouperSession.validate(s);
    Set<Stem> stems = null;
    //first find the stem.
    final Stem stem;
    try {
      stem = StemFinder.findByName(s, this.stemName, true);
    } catch (StemNotFoundException stfe) {
      if (this.failOnStemNotFound) {
        throw new QueryException("Stem not found: '" + this.stemName + "'");
      }
      //if not found, and not supposed to fail, then just return
      return new HashSet<Stem>();
    }

    final QueryOptions queryOptions = QueryOptions.create(this.sortString, this.ascending, this.pageNumber, this.pageSize);

    //based on which children, find them
    stems = (Set) GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        return stem.getChildStems(StemsInStemFilter.this.scope, queryOptions);

      }
      
    });
    
    return stems;
  }

  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the ascending
   */
  public Boolean getAscending() {
    return this.ascending;
  }

  /**
   * page number 1 indexed if paging
   * @return the pageNumber
   */
  public Integer getPageNumber() {
    return this.pageNumber;
  }

  /**
   * page size if paging
   * @return the pageSize
   */
  public Integer getPageSize() {
    return this.pageSize;
  }

  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }

  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param ascending1 the ascending to set
   */
  public void setAscending(Boolean ascending1) {
    this.ascending = ascending1;
  }

  /**
   * page number 1 indexed if paging
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(Integer pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  /**
   * page size if paging
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(Integer pageSize1) {
    this.pageSize = pageSize1;
  }

  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }

}


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
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
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
  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private Boolean ascending;
  /** page number 1 indexed if paging */
  private Integer pageNumber;
  /** page size if paging */
  private Integer pageSize;
  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;


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

  /**
   * {@link QueryFilter} that returns stems matching the specified
   * name.
   * <p>
   * This performs a substring, lowercased query against <i>name</i>,
   * <i>displayName</i>, <i>extension</i> and <i>displayExtension</i>.
   * </p>
   * @param   name  Find stems matching this name.
   * @param   ns    Restrict results to within this stem.
   * @param theSortString 
   * @param theAscending 
   * @param thePageNumber 
   * @param thePageSize 
   */
  public StemNameAnyFilter(String name, Stem ns, String theSortString, Boolean theAscending, Integer thePageNumber, Integer thePageSize) {
    this.name = name;
    this.ns   = ns;
    this.sortString = theSortString;
    this.ascending = theAscending;
    this.pageNumber = thePageNumber;
    this.pageSize = thePageSize;
  } // public StemNameAnyFilter(name, ns)


  // Public Instance Methods



  public Set getResults(GrouperSession s) 
    throws QueryException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set results;

    QueryOptions queryOptions = QueryOptions.create(this.sortString, this.ascending, this.pageNumber, this.pageSize);
    
    if (ns.isRootStem()) {
      results = removeRootStem(GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(this.name, null, queryOptions));
    } else {
      results = GrouperDAOFactory.getFactory().getStem().findAllByApproximateNameAny(this.name, getStringForScope(ns), queryOptions);
    }
    return results;
  } // public Set getResults(s)


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


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
/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectSubjectWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * find object of multiple types, and allow paging
 * @author mchyzer
 *
 */
public class GrouperFavoriteFinder {

  
  
  /**
   * which types to look for (empty means all)
   */
  private Set<GrouperFavoriteFinderType> grouperFavoriteFinderTypes = new HashSet<GrouperFavoriteFinderType>();
  
  /**
   * add grouper object finder type
   * @param grouperFavoriteFinderType1
   * @return this for chaining
   */
  public GrouperFavoriteFinder addGrouperFavoriteFinderType(GrouperFavoriteFinderType grouperFavoriteFinderType1) {
    this.grouperFavoriteFinderTypes.add(grouperFavoriteFinderType1);
    return this;
  }

  /**
   * assign grouper object finder types (null or empty is all)
   * @param grouperObjectFinderTypes1
   * @return this for chaining
   */
  public GrouperFavoriteFinder assignGrouperObjectFinderType(Collection<GrouperFavoriteFinderType> grouperObjectFinderTypes1) {
    
    if (grouperObjectFinderTypes1 == null) {
      grouperObjectFinderTypes1 = new HashSet<GrouperFavoriteFinderType>();
    } else {
    
      this.grouperFavoriteFinderTypes = new HashSet<GrouperFavoriteFinderType>(grouperObjectFinderTypes1);
    }
    
    return this;
  }
  
  /**
   * type of objects to get
   *
   */
  public static enum GrouperFavoriteFinderType {

    /** search includes stems */
    stems,
    
    /** groups */
    groups,
    
    /** subjects */
    subjects,
    
    /** attribute def names */
    attributeDefNames,
    
    /** attribute defs */
    attributeDefs;

    /** all types */
    public static final Set<GrouperFavoriteFinderType> ALL_GROUPER_FINDER_TYPES =
        Collections.unmodifiableSet(GrouperUtil.toSet(stems, groups, subjects, attributeDefNames, attributeDefs));
    
    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @return the enum or null or exception if not found
     */
    public static GrouperFavoriteFinderType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
      
      return GrouperUtil.enumValueOfIgnoreCase(GrouperFavoriteFinderType.class, 
          string, exceptionOnNull);

    }
  }
  
  /**
   * 
   */
  public GrouperFavoriteFinder() {
  }

  /**
   * subject to add to queries for example for privileges
   */
  private Subject subject; 

  /**
   * subject to add to queries for example for privileges
   * @param theSubject
   * @return this for chaining
   */
  public GrouperFavoriteFinder assignSubject(Subject theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * where the attributes are stored
   */
  private String userDataGroupName;

  
  public GrouperFavoriteFinder assignUserDataGroupName(String theUserDataGroupName) {
    this.userDataGroupName = theUserDataGroupName;
    return this;
  }
  
  /**
   * if filtering names by certain strings
   */
  private String filterText;

  /**
   * if filtering names by certain strings
   * @param theFilterText
   * @return this for chaining
   */
  public GrouperFavoriteFinder assignFilterText(String theFilterText) {
    this.filterText = theFilterText;
    return this;
  }
  
  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   */
  private Boolean splitScope;

  /**
   * if the scope has spaces in it, then split by whitespace, and find results that contain all of the scope strings
   * @param theSplitScope
   * @return this for chaining
   */
  public GrouperFavoriteFinder assignSplitScope(boolean theSplitScope) {
    this.splitScope = theSplitScope;
    return this;
  }

  /**
   * if sorting / paging
   */
  private QueryOptions queryOptions;
  
  /**
   * if sorting / paging
   * @param theQueryOptions
   */
  public GrouperFavoriteFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }
  
  /**
   * find grouper objects based on the paramet
   * @return the set of objects
   */
  public Set<GrouperObject> findFavorites() {
    
    boolean findGroups = GrouperUtil.length(this.grouperFavoriteFinderTypes) == 0 ? true : 
      this.grouperFavoriteFinderTypes.contains(GrouperFavoriteFinderType.groups);
    
    boolean findStems = GrouperUtil.length(this.grouperFavoriteFinderTypes) == 0 ? true : 
      this.grouperFavoriteFinderTypes.contains(GrouperFavoriteFinderType.stems);
    
    boolean findSubjects = GrouperUtil.length(this.grouperFavoriteFinderTypes) == 0 ? true : 
      this.grouperFavoriteFinderTypes.contains(GrouperFavoriteFinderType.subjects);
    
    boolean findAttributeDefs = GrouperUtil.length(this.grouperFavoriteFinderTypes) == 0 ? true : 
      this.grouperFavoriteFinderTypes.contains(GrouperFavoriteFinderType.attributeDefs);
    
    boolean findAttributeDefNames = GrouperUtil.length(this.grouperFavoriteFinderTypes) == 0 ? true : 
      this.grouperFavoriteFinderTypes.contains(GrouperFavoriteFinderType.attributeDefNames);
    
    Set<GrouperObject> results = new LinkedHashSet<GrouperObject>();
    
    if (StringUtils.isBlank(this.userDataGroupName)) {
      throw new RuntimeException("userDataGroupName is required");
    }
    Subject theSubject = this.subject;
    if (theSubject == null) {
      theSubject = GrouperSession.staticGrouperSession().getSubject();
    }

    Set<String> filterTokensLower = new HashSet<String>();
    
    if (!StringUtils.isBlank(this.filterText)) {
      
      String filterTokenLower = this.filterText.toLowerCase();
      
      if (this.splitScope != null && this.splitScope) {
        filterTokensLower.addAll(GrouperUtil.splitTrimToSet(filterTokenLower, " "));
      } else {
        filterTokensLower.add(filterTokenLower);
      }
      
    }

    if (findGroups) {
      results.addAll(GrouperUtil.nonNull(GrouperUserDataApi.favoriteGroups(this.userDataGroupName, theSubject)));
    }
    if (findStems) {
      results.addAll(GrouperUtil.nonNull(GrouperUserDataApi.favoriteStems(this.userDataGroupName, theSubject)));
    }
    if (findAttributeDefNames) {
      results.addAll(GrouperUtil.nonNull(GrouperUserDataApi.favoriteAttributeDefNames(this.userDataGroupName, theSubject)));
    }
    if (findAttributeDefs) {
      results.addAll(GrouperUtil.nonNull(GrouperUserDataApi.favoriteAttributeDefs(this.userDataGroupName, theSubject)));
    }
    if (findSubjects) {
      Set<Member> members = GrouperUtil.nonNull(GrouperUserDataApi.favoriteMembers(this.userDataGroupName, theSubject));
      
      for (Member member : members) {
        results.add(new GrouperObjectSubjectWrapper(member.getSubject()));
      }
      
    }

    Iterator<GrouperObject> iterator = results.iterator();
    
    //if filtering, remove the ones that dont match
    if (GrouperUtil.length(filterTokensLower) > 0) {
      while (iterator.hasNext()) {
        GrouperObject grouperObject = iterator.next();
        if (!grouperObject.matchesLowerSearchStrings(filterTokensLower)) {
          iterator.remove();
        }
      }
    }
    
    if (this.queryOptions != null && this.queryOptions.getQueryPaging() != null) {
      
      //set the total record count
      this.queryOptions.getQueryPaging().setTotalRecordCount(GrouperUtil.length(results));
      this.queryOptions.setCount(new Long(GrouperUtil.length(results)));
      
      int pageNumber = this.queryOptions.getQueryPaging().getPageNumber();
      int pageSize = this.queryOptions.getQueryPaging().getPageSize();
      
      results = new LinkedHashSet<GrouperObject>(GrouperUtil.batchList(new ArrayList<GrouperObject>(results), pageSize, pageNumber-1));
      
    }
    
    return results;
  }
  
}

/*
 * @author mchyzer $Id: WsQueryFilter.java,v 1.6 2009-11-17 02:55:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * this represents a query which can be and'ed or or'ed
 */
public class WsQueryFilter {

  /** page size if paging */
  private String pageSize;
  
  /** page number 1 indexed if paging */
  private String pageNumber;
  
  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString; 
  
  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private String ascending;
  
  /**
   * page size if paging
   * @return  page size if paging
   */
  public Integer retrievePageSize() {
    return GrouperServiceUtils.integerValue(this.pageSize, "pageSize");
  }
  
  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   */
  public Boolean retrieveAscending() {
    return GrouperServiceUtils.booleanObjectValue(this.ascending, "ascending");
  }
  
  /**
   * page number 1 indexed if paging
   * @return  page number 1 indexed if paging
   */
  public Integer retrievePageNumber() {
    return GrouperServiceUtils.integerValue(this.pageNumber, "pageNumber");
  }
  
  /**
   * page size if paging
   * @return the pageSize
   */
  public String getPageSize() {
    return this.pageSize;
  }

  
  /**
   * page size if paging
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }

  
  /**
   * page number 1 indexed if paging
   * @return the pageNumber
   */
  public String getPageNumber() {
    return this.pageNumber;
  }

  
  /**
   * page number 1 indexed if paging
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  
  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }

  
  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }

  
  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }

  
  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param ascending1 the ascending to set
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }

  /** grouper session */
  @XStreamOmitField
  private GrouperSession grouperSession = null;

  /**
   * findGroupType is the WsQueryFilterType enum for which type of find is happening:  e.g.
   * FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE,   FIND_BY_GROUP_NAME_APPROXIMATE,
   * FIND_BY_TYPE, AND, OR, MINUS;
   */
  private String queryFilterType;

  /**
   * groupName search by group name (must match exactly), cannot use other
   * params with this
   */
  private String groupName;

  /**
   * if there is a group name, there shouldnt be
   */
  public void validateNoGroupName() {
    this.validateBlank("groupName", this.groupName);
  }

  /**
   * if there is no group name, there should be
   */
  public void validateHasGroupName() {
    this.validateNotBlank("groupName", this.groupName);
  }

  /**
   * stemName will return groups only in this stem
   */
  private String stemName;

  /**
   * if there is a stem name, there shouldnt be
   */
  public void validateNoStemName() {
    this.validateBlank("stemName", this.stemName);
  }

  /**
   * if there is no stem name, there should be
   */
  public void validateHasStemName() {
    this.validateNotBlank("stemName", this.stemName);
  }

  /**
   * stemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   */
  private String stemNameScope;

  /**
   * if there is a group name, there shouldnt be
   */
  public void validateNoStemNameScope() {
    this.validateBlank("stemNameScope", this.stemNameScope);
  }

  /**
   * if there is no group name, there should be
   */
  public void validateHasStemNameScope() {
    this.validateNotBlank("stemNameScope", this.stemNameScope);
  }

  /**
   * groupUuid search by group uuid (must match exactly), cannot use other
   * params with this
   */
  private String groupUuid;

  /**
   * if there is a group uuid, there shouldnt be
   */
  public void validateNoGroupUuid() {
    this.validateBlank("groupUuid", this.groupUuid);
  }

  /**
   * if there is no group uuid, there should be
   */
  public void validateHasGroupUuid() {
    this.validateNotBlank("groupUuid", this.groupUuid);
  }

  /**
   * groupAttributeValue if searching by query, this is a term that will be matched to
   * name, extension, etc
   */
  private String groupAttributeValue;

  /**
   * if there is a groupAttributeValue, there shouldnt be
   */
  public void validateNoGroupAttributeValue() {
    this.validateBlank("groupAttributeValue", this.groupAttributeValue);
  }

  /**
   * if there is no groupAttributeValue, there should be
   */
  public void validateHasGroupAttributeValue() {
    this.validateNotBlank("groupAttributeValue", this.groupAttributeValue);
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   */
  private String groupAttributeName;

  /**
   * if there is a attribute name, there shouldnt be
   */
  public void validateNoGroupAttributeName() {
    this.validateBlank("groupAttributeName", this.groupAttributeName);
  }

  /**
   * if there is no attribute name, there should be
   * @param canHavePagingSorting if can have paging/sorting
   */
  public void validateShouldHavePagingSorting(boolean canHavePagingSorting) {

    if (!canHavePagingSorting) {
      this.validateBlank("ascending", this.ascending);
      this.validateBlank("sortString", this.sortString);
      this.validateBlank("pageNumber", this.pageNumber);
      this.validateBlank("pageSize", this.pageSize);
      return;
    }
    
    //ok, can have paging/sorting, see if there is any there...
    boolean hasPaging = !StringUtils.isBlank(this.pageNumber)
      || !StringUtils.isBlank(this.pageSize);

    if (hasPaging) {
      this.validateNotBlank("pageNumber", this.pageNumber);
      this.validateNotBlank("pageSize", this.pageSize);
    }

    boolean hasSorting = !StringUtils.isBlank(this.sortString)
      || !StringUtils.isBlank(this.ascending);
    
    if (hasSorting) {
      this.validateNotBlank("sortString", this.sortString);
      //note: ascending defaults to true
    }
  }

  /**
   * if there is no attribute name, there should be
   */
  public void validateHasGroupAttributeName() {
    this.validateNotBlank("groupAttributeName", this.groupAttributeName);
  }

  /**
   * if searching by type, this is the type to search for.  not yet implemented
   */
  private String groupTypeName;

  /**
   * if there is a type, there shouldnt be
   */
  public void validateNoGroupTypeName() {
    this.validateBlank("type", this.groupTypeName);
  }

  /**
   * if there is no type, there should be
   */
  public void validateHasGroupTypeName() {
    this.validateNotBlank("type", this.groupTypeName);
  }

  /**
   * if 'and' or 'or' this is the first group, and if complement, 
   * this is the group to complement
   */
  private WsQueryFilter queryFilter0;

  /**
   * if there is a query filter 0, there shouldnt be
   */
  public void validateNoQueryFilter0() {
    if (this.queryFilter0 != null) {
      throw new WsInvalidQueryException("Query should not contain "
          + "query filter 0, but does");
    }
  }

  /**
   * if there is no query filter 0, there should be
   */
  public void validateHasQueryFilter0() {
    if (this.queryFilter0 == null) {
      throw new WsInvalidQueryException("Query should contain "
          + "query filter 0, but doesnt");
    }
  }

  /**
   * if 'and' or 'or', this is the second group
   */
  private WsQueryFilter queryFilter1;

  /**
   * if there is a query filter 1, there shouldnt be
   */
  public void validateNoQueryFilter1() {
    if (this.queryFilter1 != null) {
      throw new WsInvalidQueryException("Query should not contain "
          + "query filter 1, but does");
    }
  }

  /**
   * if there is no query filter 1, there should be
   */
  public void validateHasQueryFilter1() {
    if (this.queryFilter1 == null) {
      throw new WsInvalidQueryException("Query should contain "
          + "query filter 1, but doesnt");
    }
  }

  /**
   * make sure a value is blank, or WsInvalidQueryException
   * @param fieldName for exception
   * @param fieldValue to check if blank
   * @throws WsInvalidQueryException
   */
  private void validateBlank(String fieldName, String fieldValue)
      throws WsInvalidQueryException {
    if (StringUtils.isNotBlank(fieldValue)) {
      throw new WsInvalidQueryException("Query should not contain " + fieldName
          + ", but contains: '" + fieldValue + "'");
    }
  }

  /**
   * make sure a value is not blank, or WsInvalidQueryException
   * @param fieldName for exception
   * @param fieldValue to check if not blank
   * @throws WsInvalidQueryException
   */
  private void validateNotBlank(String fieldName, String fieldValue)
      throws WsInvalidQueryException {
    if (StringUtils.isBlank(fieldValue)) {
      throw new WsInvalidQueryException("Query should contain " + fieldName);
    }
  }

  /**
   * findGroupType is the WsQueryFilterType enum for which type of find is happening: e.g. 
   * FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE,
   * FIND_BY_TYPE, AND, OR, MINUS; 
   * @return the findGroupType
   */
  public String getQueryFilterType() {
    return this.queryFilterType;
  }

  /**
   * findGroupType is the WsQueryFilterType enum for which type of find is happening: 
   * e.g. FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE,
   * FIND_BY_TYPE, AND, OR, MINUS; 
   * @param findGroupType1 the findGroupType to set
   */
  public void setQueryFilterType(String findGroupType1) {
    this.queryFilterType = findGroupType1;
  }

  /**
   * groupName search by group name (must match exactly), cannot use other params with this 
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * groupName search by group name (must match exactly), cannot use other params with this 
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * stemName will return groups only in this stem
   * @return the stemName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * stemName will return groups only in this stem
   * @param stemName1 the stemName to set
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }

  /**
   * stemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   * @return the stemNameScope
   */
  public String getStemNameScope() {
    return this.stemNameScope;
  }

  /**
   * convert the stem scope into a stem scope
   * @param defaultScope is the default scope
   * @return the stem scope, or the default
   */
  public StemScope retrieveStemScope(StemScope defaultScope) {
    return GrouperUtil.defaultIfNull(StemScope.valueOfIgnoreCase(this.stemNameScope),
        defaultScope);
  }

  /**
   * stemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   * @param stemNameScope1 the stemNameScope to set
   */
  public void setStemNameScope(String stemNameScope1) {
    this.stemNameScope = stemNameScope1;
  }

  /**
   * groupUuid search by group uuid (must match exactly), cannot use other
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }

  /**
   * groupUuid search by group uuid (must match exactly), cannot use other
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }

  /**
   * queryTerm if searching by query, this is a term that will be matched to
   * name, extension, etc
   * @return the queryTerm
   */
  public String getQueryTerm() {
    return this.groupAttributeValue;
  }

  /**
   * queryTerm if searching by query, this is a term that will be matched to
   * name, extension, etc
   * @param queryTerm1 the queryTerm to set
   */
  public void setQueryTerm(String queryTerm1) {
    this.groupAttributeValue = queryTerm1;
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   * @return the attributeName
   */
  public String getGroupAttributeName() {
    return this.groupAttributeName;
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   * @param attributeName1 the attributeName to set
   */
  public void setGroupAttributeName(String attributeName1) {
    this.groupAttributeName = attributeName1;
  }

  /**
   * if searching by type, this is the type to search for.  not yet implemented
   * @return the theType
   */
  public String getGroupTypeName() {
    return this.groupTypeName;
  }

  /**
   * if searching by type, this is the type to search for.  not yet implemented
   * @param theType1 the theType to set
   */
  public void setGroupTypeName(String theType1) {
    this.groupTypeName = theType1;
  }

  /**
   * if 'and' or 'or' this is the first group, and if complement, 
   * this is the group to complement 
   * @return the queryFilter0
   */
  public WsQueryFilter getQueryFilter0() {
    return this.queryFilter0;
  }

  /**
   * if 'and' or 'or' this is the first group, and if complement, 
   * this is the group to complement 
   * @param theQueryFilter0 the queryFilter0 to set
   */
  public void setQueryFilter0(WsQueryFilter theQueryFilter0) {
    this.queryFilter0 = theQueryFilter0;
  }

  /**
   * if 'and' or 'or', this is the second group
   * @return the queryFilter1
   */
  public WsQueryFilter getQueryFilter1() {
    return this.queryFilter1;
  }

  /**
   * if 'and' or 'or', this is the second group
   * @param theQueryFilter1 the queryFilter1 to set
   */
  public void setQueryFilter1(WsQueryFilter theQueryFilter1) {
    this.queryFilter1 = theQueryFilter1;
  }

  /**
   * convert the query into a query filter
   * @return the query filter
   */
  public QueryFilter retrieveQueryFilter() {
    return this.retrieveQueryFilterType().retrieveQueryFilter(this);
  }

  /**
   * 
   * @return
   */
  public WsQueryFilterType retrieveQueryFilterType() {
    if (this.queryFilterType == null) {
      throw new WsInvalidQueryException("Query filter type is required");
    }
    return WsQueryFilterType.valueOfIgnoreCase(this.queryFilterType);
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    //delegate here
    return new ReflectionToStringBuilder(this).toString();
  }

  /**
   * validate the tree of queries
   */
  public void validate() {
    this.retrieveQueryFilterType().validate(this);

    //validate subfilters if they are there
    if (this.queryFilter0 != null) {
      this.queryFilter0.validate();
    }
    if (this.queryFilter1 != null) {
      this.queryFilter1.validate();
    }
  }

  /**
   * if there is no stem, return null.
   * if there is a stem, then find it, and return it.  If not found, then 
   * throw WsInvalidQueryException
   * @return the stem or null
   */
  public Stem retrieveStem() {
    if (StringUtils.isBlank(this.stemName)) {
      return null;
    }
    try {
      Stem stem = StemFinder.findByName(this.grouperSession, this.stemName, true, new QueryOptions().secondLevelCache(false));
      return stem;
    } catch (StemNotFoundException snfe) {
      throw new WsInvalidQueryException("Cant find stem: '" + this.stemName + "'");
    }
  }

  /**
   * if there is no group type name, return null
   * if there is a group type name, then find it, and return it.  If not found, then 
   * throw WsInvalidQueryException
   * @return the stem or null
   */
  @SuppressWarnings("unchecked")
  public GroupType retrieveGroupType() {
    return GrouperServiceUtils.retrieveGroupType(this.groupTypeName);
  }

  /**
   * grouper session
   * @return the grouperSession
   */
  public GrouperSession retrieveGrouperSession() {
    return this.grouperSession;
  }

  /**
   * grouper session
   * @param grouperSession1 the grouperSession to set
   */
  public void assignGrouperSession(GrouperSession grouperSession1) {
    this.grouperSession = grouperSession1;
    
    if (this.queryFilter0 != null) {
      this.queryFilter0.assignGrouperSession(grouperSession1);
    }
    if (this.queryFilter1 != null) {
      this.queryFilter1.assignGrouperSession(grouperSession1);
    }
  }

  /**
   * groupAttributeValue if searching by query, this is a term that will be matched to
   * @return the groupAttributeValue
   */
  public String getGroupAttributeValue() {
    return this.groupAttributeValue;
  }

  /**
   * groupAttributeValue if searching by query, this is a term that will be matched to
   * @param groupAttributeValue1 the groupAttributeValue to set
   */
  public void setGroupAttributeValue(String groupAttributeValue1) {
    this.groupAttributeValue = groupAttributeValue1;
  }

}

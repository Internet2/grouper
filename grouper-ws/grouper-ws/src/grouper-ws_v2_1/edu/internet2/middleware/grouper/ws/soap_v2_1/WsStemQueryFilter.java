/*
 * @author mchyzer $Id: WsStemQueryFilter.java,v 1.4 2009-03-15 06:41:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * this represents a query which can be and'ed or or'ed
 */
public class WsStemQueryFilter {

  /**
   * findStemType is the WsStemQueryFilterType enum for which type of find is happening:  e.g.
   * FIND_BY_STEM_UUID, FIND_BY_STEM_NAME, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE, 
   * FIND_BY_TYPE, AND, OR, MINUS;
   */
  private String stemQueryFilterType;

  /**
   * stemName search by stem name (must match exactly), cannot use other
   * params with this
   */
  private String stemName;

  /**
   * parentStemName will return stems only in this stem
   */
  private String parentStemName;

  /**
   * parentStemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   */
  private String parentStemNameScope;

  /**
   * stemUuid search by stem uuid (must match exactly), cannot use other
   * params with this
   */
  private String stemUuid;

  /**
   * stemAttributeValue if searching by query, this is a term that will be matched to
   * name, extension, etc
   */
  private String stemAttributeValue;

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   */
  private String stemAttributeName;

  /**
   * if 'and' or 'or' this is the first stem, and if complement, 
   * this is the stem to complement
   */
  private WsStemQueryFilter stemQueryFilter0;

  /**
   * if 'and' or 'or', this is the second stem
   */
  private WsStemQueryFilter stemQueryFilter1;

  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private String ascending;

  /** page number 1 indexed if paging */
  private String pageNumber;

  /** page size if paging */
  private String pageSize;

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

  /**
   * findStemType is the WsFindStemType enum for which type of find is happening: e.g. 
   * FIND_BY_STEM_UUID, FIND_BY_STEM_NAME, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE, 
   * FIND_BY_TYPE, AND, OR, MINUS; 
   * @return the findStemType
   */
  public String getStemQueryFilterType() {
    return this.stemQueryFilterType;
  }

  /**
   * findStemType is the WsFindStemType enum for which type of find is happening: 
   * e.g. FIND_BY_STEM_UUID, FIND_BY_STEM_NAME, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, FIND_BY_ATTRIBUTE, 
   * FIND_BY_TYPE, AND, OR, MINUS; 
   * @param findStemType1 the findStemType to set
   */
  public void setStemQueryFilterType(String findStemType1) {
    this.stemQueryFilterType = findStemType1;
  }

  /**
   * stemName search by stem name (must match exactly), cannot use other params with this 
   * @return the stemName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * stemName search by stem name (must match exactly), cannot use other params with this 
   * @param stemName1 the stemName to set
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }

  /**
   * parentStemName will return stems only in this stem
   * @return the parentStemName
   */
  public String getParentStemName() {
    return this.parentStemName;
  }

  /**
   * parentStemName will return stems only in this stem
   * @param stemName1 the parentStemName to set
   */
  public void setParentStemName(String stemName1) {
    this.parentStemName = stemName1;
  }

  /**
   * parentStemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   * @return the parentStemNameScope
   */
  public String getParentStemNameScope() {
    return this.parentStemNameScope;
  }

  /**
   * parentStemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   * @param stemNameScope1 the parentStemNameScope to set
   */
  public void setParentStemNameScope(String stemNameScope1) {
    this.parentStemNameScope = stemNameScope1;
  }

  /**
   * stemUuid search by stem uuid (must match exactly), cannot use other
   * @return the stemUuid
   */
  public String getStemUuid() {
    return this.stemUuid;
  }

  /**
   * stemUuid search by stem uuid (must match exactly), cannot use other
   * @param stemUuid1 the stemUuid to set
   */
  public void setStemUuid(String stemUuid1) {
    this.stemUuid = stemUuid1;
  }

  /**
   * queryTerm if searching by query, this is a term that will be matched to
   * name, extension, etc
   * @return the queryTerm
   */
  public String getQueryTerm() {
    return this.stemAttributeValue;
  }

  /**
   * queryTerm if searching by query, this is a term that will be matched to
   * name, extension, etc
   * @param queryTerm1 the queryTerm to set
   */
  public void setQueryTerm(String queryTerm1) {
    this.stemAttributeValue = queryTerm1;
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   * @return the attributeName
   */
  public String getStemAttributeName() {
    return this.stemAttributeName;
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   * @param attributeName1 the attributeName to set
   */
  public void setStemAttributeName(String attributeName1) {
    this.stemAttributeName = attributeName1;
  }

  /**
   * if 'and' or 'or' this is the first stem, and if complement, 
   * this is the stem to complement 
   * @return the stemQueryFilter0
   */
  public WsStemQueryFilter getStemQueryFilter0() {
    return this.stemQueryFilter0;
  }

  /**
   * if 'and' or 'or' this is the first stem, and if complement, 
   * this is the stem to complement 
   * @param theQueryFilter0 the stemQueryFilter0 to set
   */
  public void setStemQueryFilter0(WsStemQueryFilter theQueryFilter0) {
    this.stemQueryFilter0 = theQueryFilter0;
  }

  /**
   * if 'and' or 'or', this is the second stem
   * @return the stemQueryFilter1
   */
  public WsStemQueryFilter getStemQueryFilter1() {
    return this.stemQueryFilter1;
  }

  /**
   * if 'and' or 'or', this is the second stem
   * @param theQueryFilter1 the stemQueryFilter1 to set
   */
  public void setStemQueryFilter1(WsStemQueryFilter theQueryFilter1) {
    this.stemQueryFilter1 = theQueryFilter1;
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
   * stemAttributeValue if searching by query, this is a term that will be matched to
   * @return the stemAttributeValue
   */
  public String getStemAttributeValue() {
    return this.stemAttributeValue;
  }

  /**
   * stemAttributeValue if searching by query, this is a term that will be matched to
   * @param stemAttributeValue1 the stemAttributeValue to set
   */
  public void setStemAttributeValue(String stemAttributeValue1) {
    this.stemAttributeValue = stemAttributeValue1;
  }

  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }

  /**
   * page number 1 indexed if paging
   * @return the pageNumber
   */
  public String getPageNumber() {
    return this.pageNumber;
  }

  /**
   * page size if paging
   * @return the pageSize
   */
  public String getPageSize() {
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
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }

  /**
   * page number 1 indexed if paging
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  /**
   * page size if paging
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
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

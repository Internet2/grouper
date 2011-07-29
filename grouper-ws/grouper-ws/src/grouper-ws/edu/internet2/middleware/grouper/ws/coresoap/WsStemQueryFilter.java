/*
 * @author mchyzer $Id: WsStemQueryFilter.java,v 1.4 2009-03-15 06:41:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;

/**
 * this represents a query which can be and'ed or or'ed
 */
public class WsStemQueryFilter {

  /** grouper session */
  private GrouperSession grouperSession = null;

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
   * parentStemName will return stems only in this stem
   */
  private String parentStemName;

  /**
   * if there is a parent stem name, there shouldnt be
   */
  public void validateNoParentStemName() {
    this.validateBlank("parentStemName", this.parentStemName);
  }

  /**
   * if there is no parent stem name, there should be
   */
  public void validateHasParentStemName() {
    this.validateNotBlank("parentStemName", this.parentStemName);
  }

  /**
   * parentStemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   */
  private String parentStemNameScope;

  /**
   * if there is a parent stem name, there shouldnt be
   */
  public void validateNoParentStemNameScope() {
    this.validateBlank("parentStemNameScope", this.parentStemNameScope);
  }

  /**
   * if there is no parent stem name, there should be
   */
  public void validateHasParentStemNameScope() {
    this.validateNotBlank("parentStemNameScope", this.parentStemNameScope);
  }

  /**
   * stemUuid search by stem uuid (must match exactly), cannot use other
   * params with this
   */
  private String stemUuid;

  /**
   * if there is a stem uuid, there shouldnt be
   */
  public void validateNoStemUuid() {
    this.validateBlank("stemUuid", this.stemUuid);
  }

  /**
   * if there is no stem uuid, there should be
   */
  public void validateHasStemUuid() {
    this.validateNotBlank("stemUuid", this.stemUuid);
  }

  /**
   * stemAttributeValue if searching by query, this is a term that will be matched to
   * name, extension, etc
   */
  private String stemAttributeValue;

  /**
   * if there is a stemAttributeValue, there shouldnt be
   */
  public void validateNoStemAttributeValue() {
    this.validateBlank("stemAttributeValue", this.stemAttributeValue);
  }

  /**
   * if there is no stemAttributeValue, there should be
   */
  public void validateHasStemAttributeValue() {
    this.validateNotBlank("stemAttributeValue", this.stemAttributeValue);
  }

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   */
  private String stemAttributeName;

  /**
   * if there is a attribute name, there shouldnt be
   */
  public void validateNoStemAttributeName() {
    this.validateBlank("stemAttributeName", this.stemAttributeName);
  }

  /**
   * if there is no attribute name, there should be
   */
  public void validateHasStemAttributeName() {
    this.validateNotBlank("stemAttributeName", this.stemAttributeName);
  }

  /**
   * if 'and' or 'or' this is the first stem, and if complement, 
   * this is the stem to complement
   */
  private WsStemQueryFilter stemQueryFilter0;

  /**
   * if there is a stem query filter 0, there shouldnt be
   */
  public void validateNoStemQueryFilter0() {
    if (this.stemQueryFilter0 != null) {
      throw new WsInvalidQueryException("Query should not contain "
          + "stem query filter 0, but does");
    }
  }

  /**
   * if there is no stem query filter 0, there should be
   */
  public void validateHasStemQueryFilter0() {
    if (this.stemQueryFilter0 == null) {
      throw new WsInvalidQueryException("Query should contain "
          + "stem query filter 0, but doesnt");
    }
  }

  /**
   * if 'and' or 'or', this is the second stem
   */
  private WsStemQueryFilter stemQueryFilter1;

  /**
   * if there is a stem query filter 1, there shouldnt be
   */
  public void validateNoStemQueryFilter1() {
    if (this.stemQueryFilter1 != null) {
      throw new WsInvalidQueryException("Query should not contain "
          + "query filter 1, but does");
    }
  }

  /**
   * if there is no stem query filter 1, there should be
   */
  public void validateHasStemQueryFilter1() {
    if (this.stemQueryFilter1 == null) {
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
   * convert the stem scope into a stem scope
   * @param defaultScope is the default scope
   * @return the stem scope, or the default
   */
  public StemScope retrieveStemScope(StemScope defaultScope) {
    return GrouperUtil.defaultIfNull(StemScope
        .valueOfIgnoreCase(this.parentStemNameScope), defaultScope);
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
   * convert the query into a query filter
   * @return the query filter
   */
  public QueryFilter retrieveQueryFilter() {
    return this.retrieveStemQueryFilterType().retrieveQueryFilter(this);
  }

  /**
   * 
   * @return
   */
  public WsStemQueryFilterType retrieveStemQueryFilterType() {
    if (this.stemQueryFilterType == null) {
      throw new WsInvalidQueryException("Query filter type is required");
    }
    return WsStemQueryFilterType.valueOfIgnoreCase(this.stemQueryFilterType);
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
    this.retrieveStemQueryFilterType().validate(this);

    //validate subfilters if they are there
    if (this.stemQueryFilter0 != null) {
      this.stemQueryFilter0.validate();
    }
    if (this.stemQueryFilter1 != null) {
      this.stemQueryFilter1.validate();
    }
  }

  /**
   * if there is no stem, return null.
   * if there is a stem, then find it, and return it.  If not found, then 
   * throw WsInvalidQueryException
   * @return the stem or null
   */
  public Stem retrieveParentStem() {
    if (StringUtils.isBlank(this.parentStemName)) {
      return null;
    }
    try {
      Stem stem = StemFinder.findByName(this.grouperSession, this.parentStemName, true);
      return stem;
    } catch (StemNotFoundException snfe) {
      throw new WsInvalidQueryException("Cant find stem: '" + this.parentStemName + "'");
    }
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

}

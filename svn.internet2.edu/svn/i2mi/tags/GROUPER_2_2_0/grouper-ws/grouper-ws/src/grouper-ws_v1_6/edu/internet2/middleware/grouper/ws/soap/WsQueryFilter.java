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
 * @author mchyzer $Id: WsQueryFilter.java,v 1.6 2009-11-17 02:55:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;


/**
 * this represents a query which can be and'ed or or'ed
 */
public class WsQueryFilter {

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
   * stemName will return groups only in this stem
   */
  private String stemName;

  /**
   * stemNameScope
   * if searching by stem, ONE_LEVEL is for one level,
   * ALL_IN_SUBTREE will return all in sub tree. Default is ALL_IN_SUBTREE
   */
  private String stemNameScope;

  /**
   * groupUuid search by group uuid (must match exactly), cannot use other
   * params with this
   */
  private String groupUuid;

  /**
   * groupAttributeValue if searching by query, this is a term that will be matched to
   * name, extension, etc
   */
  private String groupAttributeValue;

  /**
   * if querying, this is the attribute name, or null or search
   * all attributes
   */
  private String groupAttributeName;

  /**
   * if searching by type, this is the type to search for.  not yet implemented
   */
  private String groupTypeName;

  /**
   * if 'and' or 'or' this is the first group, and if complement, 
   * this is the group to complement
   */
  private WsQueryFilter queryFilter0;

  /**
   * if 'and' or 'or', this is the second group
   */
  private WsQueryFilter queryFilter1;

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

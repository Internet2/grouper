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
/*
 * @author mchyzer
 * $Id: WsRestGetSubjectsRequest.java,v 1.1 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * bean that will be the data from rest request
 */
public class WsRestGetSubjectsRequest implements WsRequestBean {
  
  /** field is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /** returned subjects must be in this group */
  private WsGroupLookup wsGroupLookup;
  
  /** must be one of All, Effective, Immediate, Composite, NonImmediate */
  private String memberFilter;
  
  /** subject to act as instead of logged in user */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** is if the memberships should be retrieved from a certain field membership
   * of the group (certain list) */
  private String fieldName;
  
  /** T or F as to if the group detail should be returned */
  private String includeGroupDetail;
  
  /** T|F, for if the extended subject information should be
   * returned (anything more than just the id) */
  private String includeSubjectDetail;
  
  /**  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent */
  private String[] subjectAttributeNames;
  
  /** optional: reserved for future use */
  private WsParam[] params;
  
  /** sourceIds are sources to look in for memberships, or null if all */
  private String[] sourceIds;

  /** are subjects to look in */
  private WsSubjectLookup[] wsSubjectLookups;

  /** search sources with this free-form search string */
  private String searchString;
  
  /**
   * are subjects to look in
   * @return subjects
   */
  public WsSubjectLookup[] getWsSubjectLookups() {
    return this.wsSubjectLookups;
  }

  /**
   * are subjects to look in
   * @param wsSubjectLookups1
   */
  public void setWsSubjectLookups(WsSubjectLookup[] wsSubjectLookups1) {
    this.wsSubjectLookups = wsSubjectLookups1;
  }

  /**
   * sourceIds are sources to look in for memberships, or null if all
   * @return the sourceIds
   */
  public String[] getSourceIds() {
    return this.sourceIds;
  }
  
  /**
   * sourceIds are sources to look in for memberships, or null if all
   * @param sourceIds1 the sourceIds to set
   */
  public void setSourceIds(String[] sourceIds1) {
    this.sourceIds = sourceIds1;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  /**
   * returned subjects must be in this group
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  
  /**
   * returned subjects must be in this group
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  
  /**
   * must be one of All, Effective, Immediate, Composite, NonImmediate
   * @return the replaceAllExisting
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  
  /**
   * must be one of All, Effective, Immediate, Composite, NonImmediate
   * @param replaceAllExisting1 the replaceAllExisting to set
   */
  public void setMemberFilter(String replaceAllExisting1) {
    this.memberFilter = replaceAllExisting1;
  }

  
  /**
   * subject to act as instead of logged in user
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * subject to act as instead of logged in user
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  
  /**
   * is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * @return the fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  
  /**
   * is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * @param fieldName1 the fieldName to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  
  /**
   * T or F as to if the group detail should be returned
   * @return the includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  
  /**
   * T or F as to if the group detail should be returned
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  
  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @return the includeSubjectDetail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  
  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @param includeSubjectDetail1 the includeSubjectDetail to set
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  
  /**
   *  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  
  /**
   *  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }


  
  /**
   * optional: reserved for future use
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }


  
  /**
   * optional: reserved for future use
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * search sources with this free-form search string
   * @return search string
   */
  public String getSearchString() {
    return this.searchString;
  }

  /**
   * search sources with this free-form search string
   * @param searchString1
   */
  public void setSearchString(String searchString1) {
    this.searchString = searchString1;
  }

}

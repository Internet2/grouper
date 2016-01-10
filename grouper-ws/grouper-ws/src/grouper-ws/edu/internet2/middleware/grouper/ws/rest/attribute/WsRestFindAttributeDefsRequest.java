/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
/**
 * @author vsachdeva $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean in body of rest request
 */
public class WsRestFindAttributeDefsRequest implements WsRequestBean {

  /**
   * subject if looking for privileges or service role
   */
  private WsSubjectLookup subjectLookup;

  /**
   * subject if looking for privileges or service role
   * @return subject
   */
  public WsSubjectLookup getSubjectLookup() {
    return this.subjectLookup;
  }

  /**
   * subject if looking for privileges or service role
   * @param subjectLookup1
   */
  public void setSubjectLookup(WsSubjectLookup subjectLookup1) {
    this.subjectLookup = subjectLookup1;
  }

  /**
   * search string with % as wildcards will search name, display name, description
   */
  private String scope;

  /**
   * search string with % as wildcards will search name, display name, description
   * @return the scope
   */
  public String getScope() {
    return this.scope;
  }

  /**
   * search string with % as wildcards will search name, display name, description
   * @param scope1 the scope to set
   */
  public void setScope(String scope1) {
    this.scope = scope1;
  }

  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @return the splitScope
   */
  public String getSplitScope() {
    return this.splitScope;
  }

  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   * @param splitScope1 the splitScope to set
   */
  public void setSplitScope(String splitScope1) {
    this.splitScope = splitScope1;
  }

  /**
   * @return the stemScope
   */
  public String getStemScope() {
    return this.stemScope;
  }

  /**
   * @param stemScope1 the stemScope to set
   */
  public void setStemScope(String stemScope1) {
    this.stemScope = stemScope1;
  }

  /**
   * @return the parentStemId
   */
  public String getParentStemId() {
    return this.parentStemId;
  }

  /**
   * @param parentStemId1 the parentStemId to set
   */
  public void setParentStemId(String parentStemId1) {
    this.parentStemId = parentStemId1;
  }

  /**
   * find attribute defs based on these lookups
   * @return the wsAttributeDefLookups
   */
  public WsAttributeDefLookup[] getWsAttributeDefLookups() {
    return this.wsAttributeDefLookups;
  }

  /**
   * find attribute defs based on these lookups
   * @param wsAttributeDefLookups1 the wsAttributeDefLookup to set
   */
  public void setWsAttributeDefLookups(WsAttributeDefLookup[] wsAttributeDefLookups1) {
    this.wsAttributeDefLookups = wsAttributeDefLookups1;
  }

  /**
   * page size if paging on a sort filter or parent
   * @return the pageSize
   */
  public String getPageSize() {
    return this.pageSize;
  }

  /**
   * page size if paging on a sort filter or parent
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }

  /**
   * page number 1 indexed if paging on a sort filter or parent
   * @return the pageNumber
   */
  public String getPageNumber() {
    return this.pageNumber;
  }

  /**
   * page number 1 indexed if paging on a sort filter or parent
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }

  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }

  /**
   * ascending or null for ascending, F for descending.  
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }

  /**
   * ascending or null for ascending, F for descending.  
   * @param ascending1 the ascending to set
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }

  /**
   * @return the findByUuidOrName
   */
  public String getFindByUuidOrName() {
    return this.findByUuidOrName;
  }

  /**
   * @param findByUuidOrName1 the findByUuidOrName to set
   */
  public void setFindByUuidOrName(String findByUuidOrName1) {
    this.findByUuidOrName = findByUuidOrName1;
  }

  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private String splitScope;

  /**
   * find attribute defs based on these lookups
   */
  private WsAttributeDefLookup[] wsAttributeDefLookups;

  /** stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem */
  private String stemScope;

  /**
   * parent or ancestor stem of the attribute def
   */
  private String parentStemId;

  /**
   * only look by uuid or name
   */
  private String findByUuidOrName;

  /**
   * page size if paging on a sort filter or parent
   */
  private String pageSize;

  /**
   * page number 1 indexed if paging on a sort filter or parent
   */
  private String pageNumber;

  /**
   * must be an hql query field, e.g. 
   * can sort on name, displayName, extension, displayExtension
   */
  private String sortString;

  /**
   * ascending or null for ascending, F for descending.  
   */
  private String ascending;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;

  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private WsParam[] params;

  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

}

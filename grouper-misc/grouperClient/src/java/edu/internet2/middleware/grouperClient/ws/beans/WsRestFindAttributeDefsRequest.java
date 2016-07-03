/**
 * Copyright 2016 Internet2
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
 */
/**
 * @author vsachdeva
 *
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean in body of rest request
 */
public class WsRestFindAttributeDefsRequest implements WsRequestBean {

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
   * if you want to just pass in a list of uuids and/or names.
   * @return the wsAttributeDefLookups
   */
  public WsAttributeDefLookup[] getWsAttributeDefLookups() {
    return this.wsAttributeDefLookups;
  }

  /**
   * if you want to just pass in a list of uuids and/or names.
   * @param wsAttributeDefLookups1 the wsAttributeDefLookups to set
   */
  public void setWsAttributeDefLookups(
      WsAttributeDefLookup[] wsAttributeDefLookups1) {
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
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private String splitScope;

  /**
   * attribute defs to be searched upon thses lookups
   */
  private WsAttributeDefLookup[] wsAttributeDefLookups;

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

  /** privilege to be checked for the logged in user or for actAsSubject **/
  private String privilegeName;

  public String getPrivilegeName() {
    return privilegeName;
  }

  public void setPrivilegeName(String privilegeName) {
    this.privilegeName = privilegeName;
  }

}

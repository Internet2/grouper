/*******************************************************************************
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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * request bean in body of rest request
 */
@ApiModel(description = "bean that will be the data from rest request for finding attribute defs<br /><br /><b>actAsSubjectLookup</b>: If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user to act as here<br />"
    + "<br /><br /><b>subjectLookup</b>: subject if looking for privileges or service role <br />"
    + "<br /><br /><b>params</b>: optional params for this request<br />"
    + "<br /><br /><b>wsAttributeDefLookup</b>: find attribute defs based on these lookups<br />")
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

  /** privilege name to be checked for the logged in user or for actAsSubjectLookup **/
  private String privilegeName;

  /**
  *  privilege name to be checked for the logged in user or for actAsSubjectLookup
  * @return privilegeName
  */
  @ApiModelProperty(value = "Name of the privilege", example = "for groups: read, view, update, admin, optin, optout, groupAttrRead, groupAttrUpdate.  for stems: create, stemAttrRead, stemAdmin, stemView, stemAttrUpdate")
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * privilege name to be checked for the logged in user or for actAsSubjectLookup
   * @param privilegeName1
   */
  public void setPrivilegeName(String privilegeName1) {
    this.privilegeName = privilegeName1;
  }

  /**
   * search string with % as wildcards will search name, display name, description
   */
  private String scope;

  /**
   * search string with % as wildcards will search name, display name, description
   * @return the scope
   */
  @ApiModelProperty(value = "search string with % as wildcards will search name, display name, description", example = "someApp someAttributeDefExtension")
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
  @ApiModelProperty(value = "T or F, if T will split the scope by whitespace, and find attribute def names with each token.e.g. if you have a scope of \"pto permissions\", and split scope T, it will returnschool:apps:pto_app:internal:the_permissions:whatever", example = "T|F")
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
  @ApiModelProperty(value = "is if in this stem, or in any stem underneath. You must pass stemScope if you pass a stem", example = "this:stem:name")
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
  @ApiModelProperty(value = "will return attribute defs in this stem", example = "a1b2c3d4")
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
  @ApiModelProperty(value = "Page size if paging", example = "100")
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
  @ApiModelProperty(value = "Page number 1 indexed if paging", example = "1")
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
  @ApiModelProperty(value = "Must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension", example = "name | displayName | extension | displayExtension")
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
  @ApiModelProperty(value = "T or null for ascending, F for descending.  If you pass true or false, must pass a sort string", example = "T|F")
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
  @ApiModelProperty(value = "T for find by Uuid", example = "T|F")
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
  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
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
  
  /**
   * T|F default to F.  if this is T then we are doing cursor paging
   */
  private String pageIsCursor;
  
  /**
   * field that will be sent back for cursor based paging
   */
  private String pageLastCursorField;
  
  /**
   * could be: string, int, long, date, timestamp
   */
  private String pageLastCursorFieldType;
  
  /**
   * T|F
   */
  private String pageCursorFieldIncludesLastRetrieved;

  /**
   * @return the pageIsCursor
   */
  @ApiModelProperty(value = "T|F default to F.  if this is T then we are doing cursor paging", example = "T|F")
  public String getPageIsCursor() {
    return this.pageIsCursor;
  }

  /**
   * @param pageIsCursor1 the pageIsCursor to set
   */
  public void setPageIsCursor(String pageIsCursor1) {
    this.pageIsCursor = pageIsCursor1;
  }

  /**
   * @return the pageLastCursorField
   */
  @ApiModelProperty(value = "Field that will be sent back for cursor based paging", example = "abc123")
  public String getPageLastCursorField() {
    return this.pageLastCursorField;
  }

  /**
   * @param pageLastCursorField1 the pageLastCursorField to set
   */
  public void setPageLastCursorField(String pageLastCursorField1) {
    this.pageLastCursorField = pageLastCursorField1;
  }

  /**
   * @return the pageLastCursorFieldType
   */
  @ApiModelProperty(value = "Could be: string, int, long, date, timestamp", example = "string|int|long|date|timestamp")
  public String getPageLastCursorFieldType() {
    return this.pageLastCursorFieldType;
  }

  /**
   * @param pageLastCursorFieldType1 the pageLastCursorFieldType to set
   */
  public void setPageLastCursorFieldType(String pageLastCursorFieldType1) {
    this.pageLastCursorFieldType = pageLastCursorFieldType1;
  }

  /**
   * @return the pageCursorFieldIncludesLastRetrieved
   */
  @ApiModelProperty(value = "If cursor field is unique, this should be false.  If not, then should be true.  i.e. if should include the last cursor field in the next resultset", example = "T|F")
  public String getPageCursorFieldIncludesLastRetrieved() {
    return this.pageCursorFieldIncludesLastRetrieved;
  }

  /**
   * @param pageCursorFieldIncludesLastRetrieved1 the pageCursorFieldIncludesLastRetrieved to set
   */
  public void setPageCursorFieldIncludesLastRetrieved(String pageCursorFieldIncludesLastRetrieved1) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved1;
  }
  
  

}

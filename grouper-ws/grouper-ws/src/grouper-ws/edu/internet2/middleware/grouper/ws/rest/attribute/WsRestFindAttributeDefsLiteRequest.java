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

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean in body of rest request
 */
public class WsRestFindAttributeDefsLiteRequest implements WsRequestBean {

  /**
   * subjectId subject id if looking for privileges or service role
   */
  private String subjectId;

  /**
   * subjectSourceId subject source id if looking for privileges or service role
   */
  private String subjectSourceId;

  /**
   * subjectIdentifier subject identifier if looking for privileges or service role
   */
  private String subjectIdentifier;

  /**
   * subjectId subject id if looking for privileges or service role
   * @return subjectId
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subjectId subject id if looking for privileges or service role
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * subjectSourceId subject source id if looking for privileges or service role
   * @return subject source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * subjectSourceId subject source id if looking for privileges or service role
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * subjectIdentifier subject identifier if looking for privileges or service role
   * @return subjectIdentifier
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subjectIdentifier subject identifier if looking for privileges or service role
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
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

  /**
   * find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef
   * @return the uuidOfAttributeDef
   */
  public String getUuidOfAttributeDef() {
    return this.uuidOfAttributeDef;
  }

  /**
   * find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef
   * @param uuidOfAttributeDef1 the uuidOfAttributeDef to set
   */
  public void setUuidOfAttributeDef(String uuidOfAttributeDef1) {
    this.uuidOfAttributeDef = uuidOfAttributeDef1;
  }

  /**
   * find names associated with this attribute definition, mutually exclusive with idOfAttributeDef
   * @return the nameOfAttributeDef
   */
  public String getNameOfAttributeDef() {
    return this.nameOfAttributeDef;
  }

  /**
   * find names associated with this attribute definition, mutually exclusive with idOfAttributeDef
   * @param nameOfAttributeDef1 the nameOfAttributeDef to set
   */
  public void setNameOfAttributeDef(String nameOfAttributeDef1) {
    this.nameOfAttributeDef = nameOfAttributeDef1;
  }

  /**
   * @return the idIndexOfAttributeDef
   */
  public String getIdIndexOfAttributeDef() {
    return this.idIndexOfAttributeDef;
  }

  /**
   * @param idIndexOfAttributeDef1 the idIndexOfAttributeDef to set
   */
  public void setIdIndexOfAttributeDef(String idIndexOfAttributeDef1) {
    this.idIndexOfAttributeDef = idIndexOfAttributeDef1;
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
   * find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef
   */
  private String uuidOfAttributeDef;

  /**
   * find attribute def associated with this name, mutually exclusive with idOfAttributeDef
   */
  private String nameOfAttributeDef;

  /** find attribute def associated with this attribute def id index**/
  private String idIndexOfAttributeDef;

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

  /** if acting as another user */
  private String actAsSubjectId;

  /** if acting as another user */
  private String actAsSubjectIdentifier;

  /** if acting as another user */
  private String actAsSubjectSourceId;

  /** reserved for future use */
  private String paramName0;

  /** reserved for future use */
  private String paramName1;

  /** reserved for future use */
  private String paramValue0;

  /** reserved for future use */
  private String paramValue1;

  /**
   * if acting as another user
   * @return id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * if acting as another user
   * @return subject identifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * if acting as another user
   * @return source id 
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * reserved for future use
   * @return param name 0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * reserved for future use
   * @return paramname1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * reserved for future use
   * @return param value 0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * reserved for future use
   * @return param value 1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectSourceId1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }

  /**
   * reserved for future use
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * reserved for future use
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * reserved for future use
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

}

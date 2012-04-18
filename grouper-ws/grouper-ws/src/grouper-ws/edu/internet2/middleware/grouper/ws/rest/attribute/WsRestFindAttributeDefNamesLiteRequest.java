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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;


/**
 * request bean in body of rest request
 */
public class WsRestFindAttributeDefNamesLiteRequest implements WsRequestBean {

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
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @return the attributeAssignType
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }

  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   * @param attributeAssignType1 the attributeAssignType to set
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   * @return the attributeDefType
   */
  public String getAttributeDefType() {
    return this.attributeDefType;
  }

  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   * @param attributeDefType1 the attributeDefType to set
   */
  public void setAttributeDefType(String attributeDefType1) {
    this.attributeDefType = attributeDefType1;
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
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @return the wsInheritanceSetRelation
   */
  public String getWsInheritanceSetRelation() {
    return this.wsInheritanceSetRelation;
  }

  
  /**
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   * @param wsInheritanceSetRelation1 the wsInheritanceSetRelation to set
   */
  public void setWsInheritanceSetRelation(String wsInheritanceSetRelation1) {
    this.wsInheritanceSetRelation = wsInheritanceSetRelation1;
  }

  /**
   * splitScope T or F, if T will split the scope by whitespace, and find attribute def names with each token.
   * e.g. if you have a scope of "pto permissions", and split scope T, it will return 
   * school:apps:pto_app:internal:the_permissions:whatever
   */
  private String splitScope;
  
  /**
   * where can the attribute definition be assigned, e.g. any_mem, any_mem_asgn, attr_def, 
   * attr_def_asgn, group, group_asgn, imm_mem, imm_mem_asgn, mem_asgn, member, stem, stem_asgn
   */
  private String attributeAssignType;
  
  /**
   * type of attribute definition, e.g. attr, domain, limit, perm, type
   */
  private String attributeDefType;
  
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
   * wsInheritanceSetRelation if there is one wsAttributeDefNameLookup, and this is specified, then find 
   * the attribute def names which are related to the lookup by this relation, e.g. IMPLIED_BY_THIS, 
   * IMPLIED_BY_THIS_IMMEDIATE, THAT_IMPLY_THIS, THAT_IMPLY_THIS_IMMEDIATE
   */
  private String wsInheritanceSetRelation;
  
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
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
   * to lookup an attribute def name by id, mutually exclusive with attributeDefNameName
   * @return the attributeDefNameUuid
   */
  public String getAttributeDefNameUuid() {
    return this.attributeDefNameUuid;
  }


  
  /**
   * to lookup an attribute def name by id, mutually exclusive with attributeDefNameName
   * @param attributeDefNameUuid1 the attributeDefNameUuid to set
   */
  public void setAttributeDefNameUuid(String attributeDefNameUuid1) {
    this.attributeDefNameUuid = attributeDefNameUuid1;
  }


  
  /**
   * to lookup an attribute def name by name, mutually exclusive with attributeDefNameId
   * @return the attributeDefNameName
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }


  
  /**
   * to lookup an attribute def name by name, mutually exclusive with attributeDefNameId
   * @param attributeDefNameName1 the attributeDefNameName to set
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * find names associated with this attribute definition, mutually exclusive with nameOfAttributeDef
   */
  private String uuidOfAttributeDef;
  
  /**
   * find names associated with this attribute definition, mutually exclusive with idOfAttributeDef
   */
  private String nameOfAttributeDef;
  
  /**
   * to lookup an attribute def name by id, mutually exclusive with attributeDefNameName
   */
  private String attributeDefNameUuid;
  
  /**
   * to lookup an attribute def name by name, mutually exclusive with attributeDefNameId
   */
  private String attributeDefNameName;
  
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

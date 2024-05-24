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
 * @author mchyzer $Id: WsRestMemberChangeSubjectRequest.java,v 1.1 2008-10-21 03:51:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * request bean for rest member change subject lite
 */
@ApiModel(description = "bean that will be the data from rest request for member change subject<br /><br /><b>actAsSubjectLookup</b>: If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user to act as here<br />"
    + "<br /><br /><b>wsMemberChangeSubjects</b>: members to change subjects<br />"
    + "<br /><br /><b>params</b>: optional params for this request<br />")
public class WsRestMemberChangeSubjectRequest implements WsRequestBean {

  /**
   * field 
   */
  private WsSubjectLookup actAsSubjectLookup;
  /**
   * field 
   */
  private String clientVersion;
  /**
   * field 
   */
  private String includeSubjectDetail;
  /**
   * field 
   */
  private WsParam[] params;
  
  /**
   * members to change subjects
   */
  private WsMemberChangeSubject[] wsMemberChangeSubjects;
  
  /**
   * field 
   */
  private String[] subjectAttributeNames;
  /**
   * field 
   */
  private String txType;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * @return the clientVersion
   */
  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * @return the includeSubjectDetail
   */
  @ApiModelProperty(value = "If the subject detail should be returned, default to false", example = "T|F")
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * @return the subjectAttributeNames
   */
  @ApiModelProperty(value = "are the additional subject attributes (data) to return. If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated). Only certain attributes are configured to be allowed to be retrieved", example = "lastName, middleName")
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * @return the txType
   */
  @ApiModelProperty(value = "if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)", example = "UPDATE")
  public String getTxType() {
    return this.txType;
  }

  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * @param includeSubjectDetail1 the includeSubjectDetail to set
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  /**
   * members to change subjects
   * @return the members
   */
  public WsMemberChangeSubject[] getWsMemberChangeSubjects() {
    return this.wsMemberChangeSubjects;
  }

  /**
   * members to change subjects
   * @param wsMemberChangeSubjects1
   */
  public void setWsMemberChangeSubjects(WsMemberChangeSubject[] wsMemberChangeSubjects1) {
    this.wsMemberChangeSubjects = wsMemberChangeSubjects1;
  }
}

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
 * @author mchyzer
 * $Id: WsRestGroupDeleteRequest.java,v 1.1 2008-03-30 09:01:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroups(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, boolean, boolean, String[], WsParam[])
 * for method
 */
@ApiModel(description = "bean that will be the data from rest request for deleting a group<br /><br /><b>actAsSubjectLookup</b>: If allowed to act as other users (e.g. if a UI uses the Grouper WS behind the scenes), specify the user to act as here<br />"
    + "<br /><br /><b>wsGroupLookups</b>: group to be deleted<br />"
    + "<br /><br /><b>params</b>: optional params for this request<br />")
public class WsRestGroupDeleteRequest implements WsRequestBean {
  
  /** field */
  private WsGroupLookup[] wsGroupLookups;
  
  /** field */
  private String includeGroupDetail;
  
  /** field */
  private String txType;
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private WsParam[] params;
  
  /**
   * @return the clientVersion
   */
  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
  public String getClientVersion() {
    return this.clientVersion;
  }

  
  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  
  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }


  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.DELETE;
  }

  /**
   * field
   * @return the txType
   */
  @ApiModelProperty(value = "is the GrouperTransactionType for the request. If blank, defaults to NONE (will finish as much as possible).  Generally the only values for this param that make sense are NONE (or blank), and READ_WRITE_NEW.", example = "READ_WRITE_NEW")
  public String getTxType() {
    return this.txType;
  }


  
  /**
   * field
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }


  
  /**
   * field
   * @return the wsGroupLookups
   */
  public WsGroupLookup[] getWsGroupLookups() {
    return this.wsGroupLookups;
  }


  
  /**
   * field
   * @param wsGroupLookups1 the wsGroupLookups to set
   */
  public void setWsGroupLookups(WsGroupLookup[] wsGroupLookups1) {
    this.wsGroupLookups = wsGroupLookups1;
  }


  
  /**
   * field
   * @return the includeGroupDetail
   */
  @ApiModelProperty(value = "If the group detail should be returned, default to false", example = "T|F")
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }


  
  /**
   * field
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

}

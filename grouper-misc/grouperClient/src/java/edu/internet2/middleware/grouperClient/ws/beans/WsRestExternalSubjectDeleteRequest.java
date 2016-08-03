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
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * bean that will be the data from rest request
 * see GrouperServiceLogic#getGroups(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, boolean, boolean, String[], WsParam[])
 * for method
 */
public class WsRestExternalSubjectDeleteRequest {
  
  /** field */
  private WsExternalSubjectLookup[] wsExternalSubjectLookups;
  
  /** field */
  private String txType;
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private WsParam[] params;
  
  /**
   * field
   * @return the wsExternalSubjectLookups
   */
  public WsExternalSubjectLookup[] getWsExternalSubjectLookups() {
    return this.wsExternalSubjectLookups;
  }
  
  /**
   * field
   * @param wsExternalSubjectLookups1 the wsExternalSubjectLookups to set
   */
  public void setWsExternalSubjectLookups(WsExternalSubjectLookup[] wsExternalSubjectLookups1) {
    this.wsExternalSubjectLookups = wsExternalSubjectLookups1;
  }


  /**
   * @return the clientVersion
   */
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
   * field
   * @return the txType
   */
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


  

}

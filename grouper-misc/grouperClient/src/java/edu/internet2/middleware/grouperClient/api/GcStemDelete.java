/**
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
 */
/*
 * @author mchyzer
 * $Id: GcStemDelete.java,v 1.2 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a stem delete service call
 */
public class GcStemDelete {

  /** delete group lookups */
  private List<WsStemLookup> stemLookups = new ArrayList<WsStemLookup>();
  
  /**
   * add group lookup
   * @param wsStemLookup
   * @return this for chaining
   * @deprecated use addStemLookup instead
   */
  @Deprecated
  public GcStemDelete addGroupLookup(WsStemLookup wsStemLookup) {
    return this.addStemLookup(wsStemLookup);
  }
  
  /**
   * add group lookup
   * @param wsStemLookup
   * @return this for chaining
   */
  public GcStemDelete addStemLookup(WsStemLookup wsStemLookup) {
    this.stemLookups.add(wsStemLookup);
    return this;
  }
  
  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcStemDelete addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcStemDelete addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcStemDelete assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.stemLookups) == 0) {
      throw new RuntimeException("Need at least one group to delete: " + this);
    }
  }
  
  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcStemDelete assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcStemDelete assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsStemDeleteResults execute() {
    this.validate();
    WsStemDeleteResults wsStemDeleteResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestStemDeleteRequest stemDelete = new WsRestStemDeleteRequest();

      stemDelete.setActAsSubjectLookup(this.actAsSubject);

      stemDelete.setTxType(this.txType == null ? null : this.txType.name());
      
      stemDelete.setWsStemLookups(GrouperClientUtils.toArray(this.stemLookups, 
          WsStemLookup.class));

      //add params if there are any
      if (this.params.size() > 0) {
        stemDelete.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsStemDeleteResults = (WsStemDeleteResults)
        grouperClientWs.executeService("stems", stemDelete, "stemDelete", this.clientVersion, false);
      
      String resultMessage = wsStemDeleteResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsStemDeleteResults, wsStemDeleteResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsStemDeleteResults;
    
  }
  
}

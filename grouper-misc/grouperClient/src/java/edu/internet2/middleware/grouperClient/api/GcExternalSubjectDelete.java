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
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsExternalSubjectDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsExternalSubjectLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestExternalSubjectDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a external subject delete web service call
 */
public class GcExternalSubjectDelete {

  /** delete external subject lookups */
  private List<WsExternalSubjectLookup> externalSubjectLookups = new ArrayList<WsExternalSubjectLookup>();
  
  /**
   * add external subject lookup
   * @param wsExternalSubjectLookup
   * @return this for chaining
   */
  public GcExternalSubjectDelete addExternalSubjectLookup(WsExternalSubjectLookup wsExternalSubjectLookup) {
    this.externalSubjectLookups.add(wsExternalSubjectLookup);
    return this;
  }
  
  /**
   * add external subject lookup
   * @param theIdentifier
   * @return this for chaining
   */
  public GcExternalSubjectDelete addIdentifier(String theIdentifier) {
    this.externalSubjectLookups.add(new WsExternalSubjectLookup(theIdentifier));
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
  public GcExternalSubjectDelete addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcExternalSubjectDelete addParam(WsParam wsParam) {
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
  public GcExternalSubjectDelete assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.externalSubjectLookups) == 0) {
      throw new RuntimeException("Need at least one external subject to delete: " + this);
    }
  }
  
  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcExternalSubjectDelete assignTxType(GcTransactionType gcTransactionType) {
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
  public GcExternalSubjectDelete assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsExternalSubjectDeleteResults execute() {
    this.validate();
    WsExternalSubjectDeleteResults wsExternalSubjectDeleteResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestExternalSubjectDeleteRequest externalSubjectDelete = new WsRestExternalSubjectDeleteRequest();

      externalSubjectDelete.setActAsSubjectLookup(this.actAsSubject);

      externalSubjectDelete.setTxType(this.txType == null ? null : this.txType.name());
      
      externalSubjectDelete.setWsExternalSubjectLookups(GrouperClientUtils.toArray(this.externalSubjectLookups, 
          WsExternalSubjectLookup.class));

      //add params if there are any
      if (this.params.size() > 0) {
        externalSubjectDelete.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsExternalSubjectDeleteResults = (WsExternalSubjectDeleteResults)
        grouperClientWs.executeService("externalSubjects", externalSubjectDelete, 
            "externalSubjectDelete", this.clientVersion, false);
      
      String resultMessage = wsExternalSubjectDeleteResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsExternalSubjectDeleteResults, wsExternalSubjectDeleteResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsExternalSubjectDeleteResults;
    
  }
  
}

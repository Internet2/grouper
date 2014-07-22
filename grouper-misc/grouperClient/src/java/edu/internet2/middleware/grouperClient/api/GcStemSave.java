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
 * $Id: GcStemSave.java,v 1.3 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestStemSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a stem save web service call
 */
public class GcStemSave {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcStemSave assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** stems to save */
  private List<WsStemToSave> stemsToSave = new ArrayList<WsStemToSave>();

  /**
   * add a stem to save
   * @param wsStemToSave
   * @return this for chaining
   */
  public GcStemSave addStemToSave(WsStemToSave wsStemToSave) {
    this.stemsToSave.add(wsStemToSave);
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
  public GcStemSave addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcStemSave addParam(WsParam wsParam) {
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
  public GcStemSave assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.stemsToSave) == 0) {
      throw new RuntimeException("Stem name is required: " + this);
    }
  }
  
  /**
   * tx type for request 
   */
  private GcTransactionType txType;
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   * @throws GcWebServiceError if there is a problem
   */
  public WsStemSaveResults execute() {
    this.validate();
    WsStemSaveResults wsStemSaveResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestStemSaveRequest stemSave = new WsRestStemSaveRequest();

      stemSave.setActAsSubjectLookup(this.actAsSubject);

      stemSave.setTxType(this.txType == null ? null : this.txType.name());

      stemSave.setWsStemToSaves(GrouperClientUtils.toArray(this.stemsToSave, WsStemToSave.class));
      
      //add params if there are any
      if (this.params.size() > 0) {
        stemSave.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsStemSaveResults = (WsStemSaveResults)
        grouperClientWs.executeService("stems", stemSave, "stemSave", this.clientVersion, false);
      
      String stemSaveResultMessage = "";
      
      //try to get the inner message
      try {
        stemSaveResultMessage = wsStemSaveResults.getResults()[0].getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsStemSaveResults.getResultMetadata().getResultMessage() + "\n"
        + stemSaveResultMessage;
      
      grouperClientWs.handleFailure(wsStemSaveResults, wsStemSaveResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsStemSaveResults;
    
  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcStemSave assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
}

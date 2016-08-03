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
 * $Id: GcGroupSave.java,v 1.4 2009-03-15 08:16:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsExternalSubjectSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsExternalSubjectToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestExternalSubjectSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a external subject save web service call
 */
public class GcExternalSubjectSave {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcExternalSubjectSave assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** external subjects to save */
  private List<WsExternalSubjectToSave> externalSubjectsToSave = new ArrayList<WsExternalSubjectToSave>();

  /**
   * add a external subject to save
   * @param wsExternalSubjectToSave
   * @return external subject to save
   */
  public GcExternalSubjectSave addExternalSubjectToSave(WsExternalSubjectToSave wsExternalSubjectToSave) {
    this.externalSubjectsToSave.add(wsExternalSubjectToSave);
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
  public GcExternalSubjectSave addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcExternalSubjectSave addParam(WsParam wsParam) {
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
  public GcExternalSubjectSave assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.externalSubjectsToSave) == 0) {
      throw new RuntimeException("External subject to save is required: " + this);
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
   */
  public WsExternalSubjectSaveResults execute() {
    this.validate();
    WsExternalSubjectSaveResults wsExternalSubjectSaveResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestExternalSubjectSaveRequest externalSubjectSave = new WsRestExternalSubjectSaveRequest();

      externalSubjectSave.setActAsSubjectLookup(this.actAsSubject);

      externalSubjectSave.setTxType(this.txType == null ? null : this.txType.name());

      externalSubjectSave.setWsExternalSubjectToSaves(GrouperClientUtils.toArray(this.externalSubjectsToSave, WsExternalSubjectToSave.class));
      
      //add params if there are any
      if (this.params.size() > 0) {
        externalSubjectSave.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsExternalSubjectSaveResults = (WsExternalSubjectSaveResults)
        grouperClientWs.executeService("externalSubjects", externalSubjectSave, "externalSubjectSave", 
            this.clientVersion, false);
      
      String groupSaveResultMessage = "";
      
      //try to get the inner message
      try {
        groupSaveResultMessage = wsExternalSubjectSaveResults.getResults()[0].getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsExternalSubjectSaveResults.getResultMetadata().getResultMessage() + "\n"
        + groupSaveResultMessage;
      
      grouperClientWs.handleFailure(wsExternalSubjectSaveResults, wsExternalSubjectSaveResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsExternalSubjectSaveResults;
    
  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcExternalSubjectSave assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
  }
  
}

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
 * $Id: GcGroupSave.java,v 1.4 2009-03-15 08:16:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run an attributeDefName save web service call
 */
public class GcAttributeDefNameSave {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAttributeDefNameSave assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** attributeDefNames to save */
  private List<WsAttributeDefNameToSave> attributeDefNamesToSave = new ArrayList<WsAttributeDefNameToSave>();

  /**
   * add an attributeDefName to save
   * @param wsAttributeDefNameToSave
   * @return attributeDefName to save
   */
  public GcAttributeDefNameSave addAttributeDefNameToSave(WsAttributeDefNameToSave wsAttributeDefNameToSave) {
    this.attributeDefNamesToSave.add(wsAttributeDefNameToSave);
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
  public GcAttributeDefNameSave addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAttributeDefNameSave addParam(WsParam wsParam) {
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
  public GcAttributeDefNameSave assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.attributeDefNamesToSave) == 0) {
      throw new RuntimeException("AttributeDefName name is required: " + this);
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
  public WsAttributeDefNameSaveResults execute() {
    this.validate();
    WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAttributeDefNameSaveRequest attributeDefNameSave = new WsRestAttributeDefNameSaveRequest();

      attributeDefNameSave.setActAsSubjectLookup(this.actAsSubject);

      attributeDefNameSave.setTxType(this.txType == null ? null : this.txType.name());

      attributeDefNameSave.setWsAttributeDefNameToSaves(GrouperClientUtils.toArray(this.attributeDefNamesToSave, WsAttributeDefNameToSave.class));

      //add params if there are any
      if (this.params.size() > 0) {
        attributeDefNameSave.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAttributeDefNameSaveResults = (WsAttributeDefNameSaveResults)
        grouperClientWs.executeService("attributeDefNames", attributeDefNameSave, "attributeDefNameSave", this.clientVersion, false);
      
      String attributeDefNameSaveResultMessage = "";
      
      //try to get the inner message
      try {
        attributeDefNameSaveResultMessage = wsAttributeDefNameSaveResults.getResults()[0].getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsAttributeDefNameSaveResults.getResultMetadata().getResultMessage() + "\n"
        + attributeDefNameSaveResultMessage;
      
      grouperClientWs.handleFailure(wsAttributeDefNameSaveResults, wsAttributeDefNameSaveResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAttributeDefNameSaveResults;
    
  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAttributeDefNameSave assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    //do this in GSH to setup the attribute definition
    // new AttributeDefSave(GrouperSession.startRootSession()).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).assignToGroup(true).save();
    
    WsAttributeDefNameToSave wsAttributeDefNameToSave = new WsAttributeDefNameToSave();
    wsAttributeDefNameToSave.setSaveMode("INSERT");
    //wsGroupToSave.setWsGroupLookup(new WsGroupLookup("aStem:aGroup5", null));
    WsAttributeDefName wsAttributeDefName = new WsAttributeDefName();
    wsAttributeDefName.setDisplayExtension("an attribute def name 5");
    wsAttributeDefName.setName("stem:anAttributeDefName5");
    wsAttributeDefName.setAttributeDefName("stem:permissionDef");
    wsAttributeDefNameToSave.setWsAttributeDefName(wsAttributeDefName);
    try {
      WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = new GcAttributeDefNameSave().addAttributeDefNameToSave(wsAttributeDefNameToSave).execute();
      //prints SUCCESS_INSERTED when it works
      System.out.println(wsAttributeDefNameSaveResults.getResults()[0].getResultMetadata().getResultCode());
    } catch (GcWebServiceError gwse) {
      WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = (WsAttributeDefNameSaveResults)gwse.getContainerResponseObject();
      System.out.println(wsAttributeDefNameSaveResults.getResults()[0].getResultMetadata().getResultCode());
      
      gwse.printStackTrace();
    }
  }
  
}

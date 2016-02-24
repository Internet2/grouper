/**
 * Copyright 2016 Internet2
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
 */
/*
 * @author vsachdeva
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDef;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAttributeDefSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * class to run an attributeDef save web service call
 */
public class GcAttributeDefSave {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAttributeDefSave assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }

  /** attributeDefs to save */
  private List<WsAttributeDefToSave> attributeDefsToSave = new ArrayList<WsAttributeDefToSave>();

  /**
   * add an attributeDef to save
   * @param wsAttributeDefToSave
   * @return attributeDef to save
   */
  public GcAttributeDefSave addAttributeDefToSave(
      WsAttributeDefToSave wsAttributeDefToSave) {
    this.attributeDefsToSave.add(wsAttributeDefToSave);
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
  public GcAttributeDefSave addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }

  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAttributeDefSave addParam(WsParam wsParam) {
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
  public GcAttributeDefSave assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }

  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.attributeDefsToSave) == 0) {
      throw new RuntimeException("AttributeDef is required: " + this);
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
  public WsAttributeDefSaveResults execute() {
    this.validate();
    WsAttributeDefSaveResults wsAttributeDefSaveResults = null;
    try {

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAttributeDefSaveRequest attributeDefSave = new WsRestAttributeDefSaveRequest();

      attributeDefSave.setActAsSubjectLookup(this.actAsSubject);

      attributeDefSave.setTxType(this.txType == null ? null : this.txType.name());

      attributeDefSave.setWsAttributeDefsToSave(GrouperClientUtils
          .toArray(this.attributeDefsToSave, WsAttributeDefToSave.class));

      //add params if there are any
      if (this.params.size() > 0) {
        attributeDefSave
            .setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAttributeDefSaveResults = (WsAttributeDefSaveResults) grouperClientWs
          .executeService("attributeDefs", attributeDefSave, "attributeDefSave",
              this.clientVersion, false);

      String attributeDefSaveResultMessage = "";

      //try to get the inner message
      try {
        attributeDefSaveResultMessage = wsAttributeDefSaveResults.getResults()[0]
            .getResultMetadata().getResultMessage();

      } catch (Exception e) {
      }

      String resultMessage = wsAttributeDefSaveResults.getResultMetadata()
          .getResultMessage() + "\n"
          + attributeDefSaveResultMessage;

      grouperClientWs.handleFailure(wsAttributeDefSaveResults,
          wsAttributeDefSaveResults.getResults(), resultMessage);

    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAttributeDefSaveResults;

  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAttributeDefSave assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    WsAttributeDefToSave wsAttributeDefToSave = new WsAttributeDefToSave();
    wsAttributeDefToSave.setSaveMode("INSERT");

    WsAttributeDef wsAttributeDef1 = new WsAttributeDef();
    wsAttributeDef1.setName("test1:attributeDef1");
    wsAttributeDef1.setAttributeDefType("perm");
    wsAttributeDef1.setDescription("Perm Attribute Def");
    wsAttributeDef1.setAssignableTos(new String[] { "GROUP", "EFFECTIVE_MEMBERSHIP" });
    wsAttributeDef1.setValueType("marker");

    wsAttributeDefToSave.setWsAttributeDef(wsAttributeDef1);
    try {
      WsAttributeDefSaveResults wsAttributeDefSaveResults = new GcAttributeDefSave()
          .addAttributeDefToSave(wsAttributeDefToSave).execute();
      //prints SUCCESS_INSERTED when it works
      System.out.println(
          wsAttributeDefSaveResults.getResults()[0].getResultMetadata().getResultCode());
    } catch (GcWebServiceError gwse) {
      WsAttributeDefSaveResults wsAttributeDefSaveResults = (WsAttributeDefSaveResults) gwse
          .getContainerResponseObject();
      System.out.println(
          wsAttributeDefSaveResults.getResults()[0].getResultMetadata().getResultCode());

      gwse.printStackTrace();
    }
  }

}

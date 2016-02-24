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
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefDeleteResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAttributeDefDeleteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/**
 * class to run an attributeDef delete web service call
 */
public class GcAttributeDefDelete {

  /** delete attributeDef lookups */
  private List<WsAttributeDefLookup> attributeDefLookups = new ArrayList<WsAttributeDefLookup>();

  /**
   * add attributeDef lookup
   * @param wsAttributeDefLookup
   * @return this for chaining
   */
  public GcAttributeDefDelete addAttributeDefLookup(
      WsAttributeDefLookup wsAttributeDefLookup) {
    this.attributeDefLookups.add(wsAttributeDefLookup);
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
  public GcAttributeDefDelete addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }

  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAttributeDefDelete addParam(WsParam wsParam) {
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
  public GcAttributeDefDelete assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }

  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.attributeDefLookups) == 0) {
      throw new RuntimeException("Need at least one attributeDefName to delete: " + this);
    }
  }

  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAttributeDefDelete assignTxType(GcTransactionType gcTransactionType) {
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
  public GcAttributeDefDelete assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }

  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAttributeDefDeleteResults execute() {
    this.validate();
    WsAttributeDefDeleteResults wsAttributeDefDeleteResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAttributeDefDeleteRequest attributeDefDelete = new WsRestAttributeDefDeleteRequest();

      attributeDefDelete.setActAsSubjectLookup(this.actAsSubject);

      attributeDefDelete.setTxType(this.txType == null ? null : this.txType.name());

      attributeDefDelete
          .setWsAttributeDefLookups(GrouperClientUtils.toArray(this.attributeDefLookups,
              WsAttributeDefLookup.class));

      //add params if there are any
      if (this.params.size() > 0) {
        attributeDefDelete
            .setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAttributeDefDeleteResults = (WsAttributeDefDeleteResults) grouperClientWs
          .executeService("attributeDefs", attributeDefDelete, "attributeDefDelete",
              this.clientVersion, false);

      String resultMessage = wsAttributeDefDeleteResults.getResultMetadata()
          .getResultMessage();
      grouperClientWs.handleFailure(wsAttributeDefDeleteResults,
          wsAttributeDefDeleteResults.getResults(), resultMessage);

    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAttributeDefDeleteResults;

  }

}

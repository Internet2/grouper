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
import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefName;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributeDefNameInheritanceRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to assign an attributeDefName inheritance web service call
 */
public class GcAssignAttributeDefNameInheritance {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignAttributeDefNameInheritance assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }

  //--attributeDefNameName=attributeDefNameName0 
  //--relatedAttributeDefNameNames=relatedName0,relatedName1 
  //--assign=T|F [--replaceAllExisting=T|F] 
  //[--txType=NONE|READ_WRITE_NEW] 
  //[--actAsSubjectId=subjId] [--actAsSubjectIdentifier=subjIdent] 
  //[--actAsSubjectSource=source] 
  //[--saveResultsToFile=fileName] 
  //[--outputTemplate=somePattern] 
  //[--paramName0=name0] 
  //[--paramValue0=value1] 
  //[--paramNameX=xthParamName] 
  //[--paramValueX=xthParamValue] 
  //[--debug=true] 
  //[--clientVersion=someVersion]

  /** parent attribute def name */
  private WsAttributeDefNameLookup attributeDefNameLookup = null;

  /**
   * assign the parent attribute def name
   * @param wsAttributeDefNameLookup0
   * @return the parent
   */
  public GcAssignAttributeDefNameInheritance assignAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup0) {
    
    this.attributeDefNameLookup = wsAttributeDefNameLookup0;
    return this;
  }
  
  /** attributeDefNames as children to save */
  private List<WsAttributeDefNameLookup> relatedAttributeDefNameLookups = new ArrayList<WsAttributeDefNameLookup>();

  /**
   * add an attributeDefName to save
   * @param relatedAttributeDefNameLookup
   * @return attributeDefName to save
   */
  public GcAssignAttributeDefNameInheritance addRelatedAttributeDefNameLookup(WsAttributeDefNameLookup relatedAttributeDefNameLookup) {
    this.relatedAttributeDefNameLookups.add(relatedAttributeDefNameLookup);
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
  public GcAssignAttributeDefNameInheritance addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignAttributeDefNameInheritance addParam(WsParam wsParam) {
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
  public GcAssignAttributeDefNameInheritance assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * if we are assigning or unassigning
   */
  private Boolean assign;

  /**
   * if we are assigning or unassigning
   * @param isAssign
   * @return this for chaining
   */
  public GcAssignAttributeDefNameInheritance assign(Boolean isAssign) {
    this.assign = isAssign;
    return this;
  }
  
  /**
   * if it is an assignment, if we are replacing existing assignments
   */
  private Boolean replaceAllExisting;
  
  /**
   * if it is an assignment, if we are replacing existing assignments
   * @param replaceAllExisting1
   * @return this for chaining
   */
  public GcAssignAttributeDefNameInheritance assignReplaceAllExisting(Boolean replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.relatedAttributeDefNameLookups) == 0) {
      throw new RuntimeException("Related AttributeDefName name is required: " + this);
    }
    if (this.attributeDefNameLookup == null || (GrouperClientUtils.isBlank(this.attributeDefNameLookup.getName())
        && GrouperClientUtils.isBlank(this.attributeDefNameLookup.getUuid()))) {
      throw new RuntimeException("Related AttributeDefName name is required: " + this);
    }
    if (this.assign == null) {
      throw new RuntimeException("Assign is required, true means you are assigning, false means you are removing a direct assignment");
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
  public WsAssignAttributeDefNameInheritanceResults execute() {
    this.validate();
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignAttributeDefNameInheritanceRequest assignAttributeDefNameInheritance = new WsRestAssignAttributeDefNameInheritanceRequest();

      assignAttributeDefNameInheritance.setActAsSubjectLookup(this.actAsSubject);

      assignAttributeDefNameInheritance.setTxType(this.txType == null ? null : this.txType.name());

      assignAttributeDefNameInheritance.setRelatedWsAttributeDefNameLookups(GrouperClientUtils.toArray(this.relatedAttributeDefNameLookups, WsAttributeDefNameLookup.class));

      assignAttributeDefNameInheritance.setWsAttributeDefNameLookup(this.attributeDefNameLookup);

      if (this.assign != null) {
        assignAttributeDefNameInheritance.setAssign(this.assign ? "T" : "F");
      }
      
      if (this.replaceAllExisting != null) {
        assignAttributeDefNameInheritance.setReplaceAllExisting(this.replaceAllExisting ? "T" : "F");
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
        assignAttributeDefNameInheritance.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAssignAttributeDefNameInheritanceResults = (WsAssignAttributeDefNameInheritanceResults)
        grouperClientWs.executeService("attributeDefNames", assignAttributeDefNameInheritance, "assignAttributeDefNameInheritance", this.clientVersion, false);
      
      String attributeDefNameSaveResultMessage = "";
      
      //try to get the message
      try {
        attributeDefNameSaveResultMessage = wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsAssignAttributeDefNameInheritanceResults.getResultMetadata().getResultMessage() + "\n"
        + attributeDefNameSaveResultMessage;
      
      grouperClientWs.handleFailure(wsAssignAttributeDefNameInheritanceResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignAttributeDefNameInheritanceResults;
    
  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAssignAttributeDefNameInheritance assignTxType(GcTransactionType gcTransactionType) {
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

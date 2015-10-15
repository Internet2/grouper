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
 * $Id: GcGetMemberships.java,v 1.1 2009-12-19 21:38:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignActionsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetAttributeAssignActionsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get attribute assign actions web service call
 */
public class GcGetAttributeAssignActions {                                                                                                                                                                                                                                                                                                                                                                                                                                                           
  
  /** client version */
  private String clientVersion;

  
  /** to query, or none to query all actions */
  private Set<String> actions = new LinkedHashSet<String>();
  
  /**
   * 
   * @param action
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAction(String action) {
    this.actions.add(action);
    return this;
  }  
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetAttributeAssignActions assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
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
  public GcGetAttributeAssignActions addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addParam(WsParam wsParam) {
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
  public GcGetAttributeAssignActions assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  

 
  /** attributeDef names to query */
  private Set<String> namesOfAttributeDefs = new LinkedHashSet<String>();

  /** attributeDef uuids to query */
  private Set<String> uuidsOfAttributeDefs = new LinkedHashSet<String>();

  /** attributeDef id indexes to query */
  private Set<Long> idIndexesOfAttributeDefs = new LinkedHashSet<Long>();  
  
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.namesOfAttributeDefs) == 0 && GrouperClientUtils.length(this.uuidsOfAttributeDefs) == 0 &&
    	GrouperClientUtils.length(this.idIndexesOfAttributeDefs) == 0 ) {
      throw new RuntimeException("atleast one of the attributeDefNames, attributeDefUuids and attributeDefIdIndexes "
        + "is required : " + this);
    }
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetAttributeAssignActionsResults execute() {
    this.validate();
    WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      
      WsRestGetAttributeAssignActionsRequest getAttributeAssignActions = new WsRestGetAttributeAssignActionsRequest();
      
      getAttributeAssignActions.setActAsSubjectLookup(this.actAsSubject);

      
      {
        //########### ATTRIBUTE DEFS
        List<WsAttributeDefLookup> attributeDefLookups = new ArrayList<WsAttributeDefLookup>();
        //add names and/or uuids
        for (String nameOfAttributeDef : this.namesOfAttributeDefs) {
          attributeDefLookups.add(new WsAttributeDefLookup(nameOfAttributeDef, null));
        }
        for (String uuidOfAttributeDef : this.uuidsOfAttributeDefs) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, uuidOfAttributeDef));
        }
        for (Long idIndexOfAttributeDef : this.idIndexesOfAttributeDefs) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, null, idIndexOfAttributeDef.toString()));
        }
        if (GrouperClientUtils.length(attributeDefLookups) > 0) {
        	getAttributeAssignActions.setWsAttributeDefLookups(GrouperClientUtils.toArray(attributeDefLookups, WsAttributeDefLookup.class));
        }
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
    	  getAttributeAssignActions.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (GrouperClientUtils.length(this.actions) > 0) {
    	  getAttributeAssignActions.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetAttributeAssignActionsResults = (WsGetAttributeAssignActionsResults)
        grouperClientWs.executeService("attributeAssignActions", 
        		getAttributeAssignActions, "getAttributeAssignActions", this.clientVersion, true);
      
      String resultMessage = wsGetAttributeAssignActionsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetAttributeAssignActionsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetAttributeAssignActionsResults;
    
  }


  /**
   * set the attributedef name
   * @param nameOfAttributeDef
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefName(String nameOfAttributeDef) {
    this.namesOfAttributeDefs.add(nameOfAttributeDef);
    return this;
  }

  /**
   * set the attributedef uuid
   * @param uuidOfAttributeDef
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefUuid(String uuidOfAttributeDef) {
    this.uuidsOfAttributeDefs.add(uuidOfAttributeDef);
    return this;
  }

  /**
   * set the attributedef id index
   * @param idIndexOfATtributeDef
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefIdIndex(Long idIndexOfATtributeDef) {
    this.idIndexesOfAttributeDefs.add(idIndexOfATtributeDef);
    return this;
  }

}

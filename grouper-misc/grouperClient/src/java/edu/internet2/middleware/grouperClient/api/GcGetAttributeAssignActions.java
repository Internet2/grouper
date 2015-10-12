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
  private Set<String> attributeDefNames = new LinkedHashSet<String>();

  /** attributeDef uuids to query */
  private Set<String> attributeDefUuids = new LinkedHashSet<String>();

  /** attributeDef id indexes to query */
  private Set<Long> attributeDefIdIndexes = new LinkedHashSet<Long>();  
  
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.attributeDefNames) == 0 && GrouperClientUtils.length(this.attributeDefUuids) == 0 &&
    	GrouperClientUtils.length(this.attributeDefIdIndexes) == 0 ) {
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
        for (String attributeDefName : this.attributeDefNames) {
          attributeDefLookups.add(new WsAttributeDefLookup(attributeDefName, null));
        }
        for (String attributeDefUuid : this.attributeDefUuids) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, attributeDefUuid));
        }
        for (Long attributeDefIdIndex : this.attributeDefIdIndexes) {
          attributeDefLookups.add(new WsAttributeDefLookup(null, null, attributeDefIdIndex.toString()));
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
   * @param theAttributeDefName
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefName(String theAttributeDefName) {
    this.attributeDefNames.add(theAttributeDefName);
    return this;
  }

  /**
   * set the attributedef uuid
   * @param theAttributeDefUuid
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefUuid(String theAttributeDefUuid) {
    this.attributeDefUuids.add(theAttributeDefUuid);
    return this;
  }

  /**
   * set the attributedef id index
   * @param theAttributeDefIdIndex
   * @return this for chaining
   */
  public GcGetAttributeAssignActions addAttributeDefIdIndex(Long theAttributeDefIdIndex) {
    this.attributeDefIdIndexes.add(theAttributeDefIdIndex);
    return this;
  }

}

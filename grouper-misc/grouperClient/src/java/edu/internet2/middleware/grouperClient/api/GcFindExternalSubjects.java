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
 * $Id: GcFindExternalSubjects.java,v 1.5 2009-12-19 21:38:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsExternalSubjectLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindExternalSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindExternalSubjectsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run find external subject
 */
public class GcFindExternalSubjects {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcFindExternalSubjects assignClientVersion(String theClientVersion) {
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
  public GcFindExternalSubjects addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcFindExternalSubjects addParam(WsParam wsParam) {
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
  public GcFindExternalSubjects assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
  }
  
  /** external subject identifiers to query */
  private Set<String> identifiers = new LinkedHashSet<String>();
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsFindExternalSubjectsResults execute() {
    this.validate();
    WsFindExternalSubjectsResults wsFindExternalSubjectsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestFindExternalSubjectsRequest findExternalSubjects = new WsRestFindExternalSubjectsRequest();

      findExternalSubjects.setActAsSubjectLookup(this.actAsSubject);

      //add params if there are any
      if (this.params.size() > 0) {
        findExternalSubjects.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      List<WsExternalSubjectLookup> externalSubjectLookups = new ArrayList<WsExternalSubjectLookup>();
      //add names and/or uuids
      for (String identifier : this.identifiers) {
        externalSubjectLookups.add(new WsExternalSubjectLookup(identifier));
      }
      findExternalSubjects.setWsExternalSubjectLookups(GrouperClientUtils.toArray(externalSubjectLookups, WsExternalSubjectLookup.class));

      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsFindExternalSubjectsResults = (WsFindExternalSubjectsResults)
        grouperClientWs.executeService("externalSubjects", findExternalSubjects, "findExternalSubjects", this.clientVersion, true);
      
      String resultMessage = wsFindExternalSubjectsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsFindExternalSubjectsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsFindExternalSubjectsResults;
    
  }

  /**
   * set the identifier
   * @param theIdentifier
   * @return this for chaining
   */
  public GcFindExternalSubjects addIdentifier(String theIdentifier) {
    this.identifiers.add(theIdentifier);
    return this;
  }
  
}

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
 * $Id: GcGetGrouperPrivilegesLite.java,v 1.2 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get grouper privileges web service call
 */
public class GcGetGrouperPrivilegesLite {

  /** group name to query privileges */
  private String groupName;
  
  /** privilege type, e.g. access or naming */
  private String privilegeType;

  /**
   * assign privilege type
   * @param thePrivilegeType
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignPrivilegeType(String thePrivilegeType) {
    this.privilegeType = thePrivilegeType;
    return this;
  }
  
  /** privilege name, e.g. admin */
  private String privilegeName;

  /**
   * assign the privilege name to query
   * @param thePrivilegeName
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignPrivilegeName(String thePrivilegeName) {
    this.privilegeName = thePrivilegeName;
    return this;
  }
   
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /** stem name to query about privileges */
  private String stemName;
  
  /**
   * set the stem name to query privs
   * @param theStemName
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }
  
  /** subject lookups */
  private WsSubjectLookup subjectLookup = null;

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookup = wsSubjectLookup;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.params) > 2) {
      throw new RuntimeException("Params can only be size 2 for this request");
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetGrouperPrivilegesLite assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetGrouperPrivilegesLiteResult execute() {
    this.validate();
    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetGrouperPrivilegesLiteRequest wsGetGrouperPrivilegesLite = new WsRestGetGrouperPrivilegesLiteRequest();

      if (this.actAsSubject != null) {
        wsGetGrouperPrivilegesLite.setActAsSubjectId(this.actAsSubject.getSubjectId());
        wsGetGrouperPrivilegesLite.setActAsSubjectIdentifier(this.actAsSubject.getSubjectIdentifier());
        wsGetGrouperPrivilegesLite.setActAsSubjectSourceId(this.actAsSubject.getSubjectSourceId());
      }

      if (this.includeGroupDetail != null) {
        wsGetGrouperPrivilegesLite.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        wsGetGrouperPrivilegesLite.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      wsGetGrouperPrivilegesLite.setGroupName(this.groupName);
      wsGetGrouperPrivilegesLite.setStemName(this.stemName);
      
      wsGetGrouperPrivilegesLite.setPrivilegeType(this.privilegeType);
      wsGetGrouperPrivilegesLite.setPrivilegeName(this.privilegeName);
      
      if (this.subjectAttributeNames.size() > 0) {
        wsGetGrouperPrivilegesLite.setSubjectAttributeNames(
            GrouperClientUtils.join(this.subjectAttributeNames.iterator(), ','));
      }
      
      if (this.subjectLookup != null) {
        wsGetGrouperPrivilegesLite.setSubjectId(this.subjectLookup.getSubjectId());
        wsGetGrouperPrivilegesLite.setSubjectIdentifier(this.subjectLookup.getSubjectIdentifier());
        wsGetGrouperPrivilegesLite.setSubjectId(this.subjectLookup.getSubjectId());
      }

      //add params if there are any
      if (this.params.size() > 0) {
        wsGetGrouperPrivilegesLite.setParamName0(this.params.get(0).getParamName());
        wsGetGrouperPrivilegesLite.setParamValue0(this.params.get(0).getParamValue());
      }
      if (this.params.size() > 1) {
        wsGetGrouperPrivilegesLite.setParamName1(this.params.get(1).getParamName());
        wsGetGrouperPrivilegesLite.setParamValue1(this.params.get(1).getParamValue());
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetGrouperPrivilegesLiteResult = (WsGetGrouperPrivilegesLiteResult)
        grouperClientWs.executeService("grouperPrivileges", 
            wsGetGrouperPrivilegesLite, "getGrouperPrivilegesLite", this.clientVersion, true);
      
      String resultMessage = wsGetGrouperPrivilegesLiteResult.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetGrouperPrivilegesLiteResult, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetGrouperPrivilegesLiteResult;
    
  }
  
}
